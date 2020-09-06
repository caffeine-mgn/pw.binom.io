package pw.binom.thread

import pw.binom.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

class WorkerFuture<T> : Future<T> {

    internal val lock = ReentrantLock()
    internal val con = lock.newCondition()
    private var done = AtomicBoolean()
    private var _result: T? = null
    private var _exception: Throwable? = null

    fun resume(result: Result<T>) {
        lock.lock {
            if (!done.compareAndSet(false, true))
                throw IllegalStateException()
            if (result.isSuccess)
                _result = result.getOrNull()
            else
                _exception = result.exceptionOrNull()
            con.signalAll()
        }
    }

    override fun <R> consume(func: (Result<T>) -> R): R =
            lock.lock {
                if (!this.done.get()) {
                    con.await()
                }
                if (!done.get())
                    throw IllegalStateException()
                val res = if (_exception != null) {
                    Result.failure(_exception!!)
                } else {
                    Result.success(_result as T)
                }
                func(res)
            }

    override val isSuccess: Boolean
        get() = consume { it.isSuccess }

    override val resultOrNull: T?
        get() = consume { it }.getOrNull()

    override val exceptionOrNull: Throwable?
        get() = consume { it }.exceptionOrNull()
    override val isDone: Boolean
        get() = done.get()
}