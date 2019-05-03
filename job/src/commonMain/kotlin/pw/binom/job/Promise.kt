package pw.binom.job

import pw.binom.Lock
import pw.binom.Thread
import pw.binom.use

interface FuturePromise<T> {
    val isFinished: Boolean
    val isError: Boolean
    val result: T
    val exception: Throwable

    /**
     * Blocks current thread until Promise will be done
     *
     * @param success call when promise done success
     * @param exception call when promise done with exception
     */
    fun consume(success: (T) -> Unit, exception: (Throwable) -> Unit)
}

fun FuturePromise<*>.join() {
    while (!isFinished) {
        Thread.sleep(1)
    }
}

class Promise<T : Any?> {

    companion object {
        fun <T> resolve(value: T): Promise<T> {
            val p = Promise<T>()
            p.resume(value)
            return p
        }
    }

    private var _result: T? = null
    private var done = false
    private var _error: Throwable? = null
    private var thenFunc: ((T) -> Unit)? = null
    private var errorFunc: ((Throwable) -> Unit)? = null
    private var called = false
    private val lock = Lock()

    val isFinished: Boolean
        get() = lock.use { done }

    val isError: Boolean
        get() = lock.use {
            if (!done)
                throw IllegalStateException("Promise not finished")
            return isError
        }

    val error: Throwable
        get() = lock.use {
            if (!done)
                throw IllegalStateException("Promise not finished")
            return _error!!
        }

    val result: T
        get() = lock.use {
            if (!done)
                throw IllegalStateException("Promise not finished")
            return _result as T
        }

    fun then(func: ((T) -> Unit)?, error: ((Throwable) -> Unit)?) {
        lock.use {
            if (called)
                throw IllegalStateException("Promise already called")
            thenFunc = func
            errorFunc = error

            if (done)
                thenFunc!!(_result as T)
        }
    }

    fun resume(value: T) {
        lock.use {
            done = true
            _result = value
            if (thenFunc != null) {
                called = true
                thenFunc!!(value)
            }
        }
    }

    fun exception(exception: Throwable) {
        lock.use {
            done = true
            _error = exception
            if (errorFunc != null) {
                called = true
                errorFunc!!(exception)
            }
        }
    }

    fun join() {
        while (!isFinished) {
            Thread.sleep(1)
        }
    }
}