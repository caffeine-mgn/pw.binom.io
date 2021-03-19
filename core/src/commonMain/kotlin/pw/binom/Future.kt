package pw.binom

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference

interface Future2<T> {

    companion object {
        fun <T> success(result: T): Future2<T> = SuccessFuture2(result)
    }

    /**
     * Function will call on Future has done. Can be called on other thread. [func] will be freeze
     */
    var onDone: ((Result<T>) -> Unit)?

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
    override var onDone: ((Result<T>) -> Unit)?
        get() = null
        set(value) {
            value?.invoke(Result.success(result))
        }
}

class BaseFuture<T> : Future2<T> {

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

    private var onDoneEvent by AtomicReference<((Result<T>) -> Unit)?>(null)
    private var result by AtomicReference<Any?>(null)
    override var onDone: ((Result<T>) -> Unit)?
        get() = onDoneEvent
        set(value) {
            onDoneEvent = null
            val r = result
            if (r != null) {
                if (value != null) {
                    val res = if (isSuccess) Result.success(r as T) else Result.failure(r as Throwable)
                    value.invoke(res)
                }
                return
            }
            onDoneEvent = value
        }

    fun resume(result: Result<T>) {
        if (!_isDone.compareAndSet(false, true)) {
            throw IllegalStateException("Future already resumed")
        }
        _isSuccess.value = result.isSuccess
        this.result = if (result.isSuccess) result.getOrNull() else result.exceptionOrNull()
        val r = onDoneEvent
        onDoneEvent = null
        r?.invoke(result)
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