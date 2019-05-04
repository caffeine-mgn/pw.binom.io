package pw.binom.job

import pw.binom.Lock
import pw.binom.Thread
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import pw.binom.use

interface FuturePromise<T:Any?> {
    val isFinished: Boolean
    val isError: Boolean
    val result: T
    val exception: Throwable
}

/**
 * Blocks current thread until Promise will be done
 *
 * @param success call when promise done success
 * @param exception call when promise done with exception
 */
fun <T:Any?> FuturePromise<T>.consume(success: (T) -> Unit, exception: (Throwable) -> Unit){
    join()
    if (isError)
        exception(this.exception)
    else
        success(result)
}

/**
 * Blocks current thread until Promise will be done. Execution result can be get from [FuturePromise.isFinished] and others
 */
fun FuturePromise<*>.join() {
    while (!isFinished) {
        Thread.sleep(1)
    }
}

/**
 * Blocks current thread until Promise will be done. When promise done function returns result of promise or throw exception
 *
 * @return Result of Promise
 */
fun <T>FuturePromise<T>.await():T{
    join()
    if (isError)
        throw exception
    else
        return result
}

class Promise<T:Any?> : FuturePromise<T> {

    private val done = AtomicBoolean(false)
    private val withException = AtomicBoolean(false)

    private val resultObj = AtomicReference<T?>(null)
    private val errorObj = AtomicReference<Throwable?>(null)

    override val exception: Throwable
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise not ready")
            if (!isError)
                throw IllegalStateException("Promise not throws exception")
            return errorObj.value!!
        }

    override val isError: Boolean
        get() = withException.value

    override val isFinished: Boolean
        get() = done.value

    override val result: T
        get() {
            if (!isFinished)
                throw IllegalStateException("Promise not ready")
            if (!isError)
                throw IllegalStateException("Promise throws exception")
            return resultObj.value as T
        }

    fun resume(result: T) {
        (result as Any?)?.doFreeze()
        resultObj.value = result
        done.value = true
    }

    fun exception(exception: Throwable) {
        exception.doFreeze()
        errorObj.value = exception
        done.value = true
    }
}