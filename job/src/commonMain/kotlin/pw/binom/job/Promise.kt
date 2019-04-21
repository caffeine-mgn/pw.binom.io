package pw.binom.job

class Promise<T : Any?> {
    private var _result: T? = null
    private var done = false
    private var _error: Throwable? = null
    private var thenFunc: ((T) -> Unit)? = null
    private var errorFunc: ((Throwable) -> Unit)? = null
    private var called = false

    val isFinished: Boolean
        get() = done

    val isError: Boolean
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise not finished")
            return isError
        }

    val error: Throwable
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise not finished")
            return _error!!
        }

    val result: T
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise not finished")
            return _result as T
        }

    fun then(func: ((T) -> Unit)?, error: ((Throwable) -> Unit)?) {
        if (called)
            throw IllegalStateException("Promise already called")
        thenFunc = func
        errorFunc = error

        if (done)
            thenFunc!!(_result as T)
    }

    fun resume(value: T) {
        done = true
        _result = value
        if (thenFunc != null) {
            called = true
            thenFunc!!(value)
        }
    }

    fun exception(exception: Throwable) {
        done = true
        _error = exception
        if (errorFunc != null) {
            called = true
            errorFunc!!(exception)
        }
    }
}