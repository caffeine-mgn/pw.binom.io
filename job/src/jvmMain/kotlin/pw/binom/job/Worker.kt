package pw.binom.job

import pw.binom.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

actual class Worker : Closeable {

    private val native = Executors.newFixedThreadPool(1)

    actual fun <P, R> execute(param: () -> P, task: (P) -> R): FuturePromise<R> {
        val p = CompletableFuture<R>()

        val argument = try {
            param()
        } catch (e: Throwable) {
            p.completeExceptionally(e)
            return FuturePromiseImpl(p)
        }

        native.submit {
            try {
                p.complete(task(argument))
            } catch (e: Throwable) {
                p.completeExceptionally(e)
            }
        }
        return FuturePromiseImpl(p)
    }

    override fun close() {
        native.shutdown()
        native.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
    }

    actual companion object
}

private class FuturePromiseImpl<T>(private val future: CompletableFuture<T>) : FuturePromise<T> {

    override val isFinished: Boolean
        get() = _isDone

    override val isError: Boolean
        get(){
            if (!isFinished)
                throw IllegalStateException("Promise not ready")
            return _isError
        }

    override val result: T
        get(){
            if (!isFinished)
                throw IllegalStateException("Promise not ready")
            if (isError)
                throw IllegalStateException("Promise throws Exception")
            return _result as T
        }

    override val exception: Throwable
        get(){
            if (!isFinished)
                throw IllegalStateException("Promise not ready")
            if (isError)
                throw IllegalStateException("Promise not throws Exception")
            return _error!!
        }

    private var _success: ((T) -> Unit)? = null
    private var _exception: ((Throwable) -> Unit)? = null

    @Volatile
    private var _isDone = false

    @Volatile
    private var _isError = false

    private var _result: T? = null
    private var _error: Throwable? = null

    init {
        future.whenComplete { r, e ->
            _isDone = true
            _isError = future.isCompletedExceptionally
            _result=r
            _error=e

            if (future.isCompletedExceptionally) {
                _exception?.invoke(e)
            } else {
                _success?.invoke(r)
            }
        }
    }

}