package pw.binom

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference

interface Future2<T> {

    companion object {
        fun <T> success(result: T): Future2<T> = SuccessFuture2(result)
        fun <T> fail(result: Throwable): Future2<T> = FailFuture2(result)
    }

    /**
     * Getting current result value. If Future not ready will throw FutureNotReadyException
     */
    val resultOrNull: T?

    /**
     * Getting flag of success future. If Future not ready will throw FutureNotReadyException
     */
    val isSuccess: Boolean

    /**
     * Getting flag of failure future. If Future not ready will throw FutureNotReadyException
     */
    val isFailure: Boolean
        get() = !isSuccess

    /**
     * Getting current exception of future. If Future not ready will throw FutureNotReadyException
     */
    val exceptionOrNull: Throwable?

    /**
     * Getting is done of future
     */
    val isDone: Boolean

    /**
     * Throws whan future not ready
     */
    class FutureNotReadyException : IllegalStateException()
    class FutureAlreadyResumedException : IllegalStateException()
}

fun <T> Future2<T>.getOrException(): T {
    if (isFailure) {
        throw exceptionOrNull!!
    }
    return resultOrNull as T
}

private class SuccessFuture2<T>(val result: T) : Future2<T> {
    override val resultOrNull: T?
        get() = result
    override val isSuccess: Boolean
        get() = true
    override val exceptionOrNull: Throwable?
        get() = null
    override val isDone: Boolean
        get() = true
}

private class FailFuture2<T>(val result: Throwable) : Future2<T> {
    override val resultOrNull: T?
        get() = null
    override val isSuccess: Boolean
        get() = false
    override val exceptionOrNull: Throwable?
        get() = result
    override val isDone: Boolean
        get() = true
}

class NonFreezableFuture<T> : Future2<T> {
    init {
        neverFreeze()
    }

    private var result: Any? = null
    override val resultOrNull: T?
        get() = if (isSuccess) result as T else null
    override var isSuccess: Boolean = false
        get() {
            if (!isDone) {
                throw Future2.FutureNotReadyException()
            }
            return field
        }
        private set

    override val exceptionOrNull: Throwable?
        get() = if (!isSuccess) result as Throwable else null

    override var isDone: Boolean = false
        private set

    fun resume(result: Result<T>) {
        if (isDone) {
            throw Future2.FutureAlreadyResumedException()
        }
        isDone = true
        isSuccess = result.isSuccess
        this.result = if (result.isSuccess) result.getOrNull() else result.exceptionOrNull()
    }
}

class FreezableFuture<T> : Future2<T> {

    override val resultOrNull: T?
        get() {
            if (!isDone) {
                throw Future2.FutureNotReadyException()
            }
            return if (isSuccess) {
                result as T
            } else {
                null
            }
        }
    private var _isSuccess = AtomicBoolean(false)
    override val isSuccess: Boolean
        get() {
            if (!isDone) {
                throw Future2.FutureNotReadyException()
            }
            return _isSuccess.value
        }
    override val exceptionOrNull: Throwable?
        get() =
            if (isSuccess) {
                null
            } else {
                result as Throwable
            }
    private var _isDone = AtomicBoolean(false)
    override val isDone: Boolean
        get() = _isDone.value

    private var result by AtomicReference<Any?>(null)

    fun resume(result: Result<T>) {
        if (!_isDone.compareAndSet(false, true)) {
            throw Future2.FutureAlreadyResumedException()
        }
        _isSuccess.value = result.isSuccess
        this.result = if (result.isSuccess) result.getOrNull() else result.exceptionOrNull()
    }
}

interface Future<T> {
    val resultOrNull: T?
    val isSuccess: Boolean
    val isFailure: Boolean
        get() = !isSuccess
    val exceptionOrNull: Throwable?
    val isDone: Boolean

    fun <R> consume(func: (Result<T>) -> R): R

    companion object {
        fun <T> success(result: T): Future<T> = SuccessFuture(result)
    }
}

private class SuccessFuture<T>(result: T) : Future<T> {
    override val resultOrNull: T? = result
    override val isSuccess: Boolean
        get() = true
    override val exceptionOrNull: Throwable?
        get() = null

    override fun <R> consume(func: (Result<T>) -> R): R = func(Result.success(resultOrNull as T))
    override val isDone: Boolean
        get() = true
}