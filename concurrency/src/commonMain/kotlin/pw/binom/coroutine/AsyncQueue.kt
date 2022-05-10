package pw.binom.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.FrozenQueue
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException

class AsyncQueue<T> : Closeable, AsyncExchangeInput<T>, ExchangeOutput<T> {
    private val listeners = FrozenQueue<CancellableContinuation<T>>()
    private val values = FrozenQueue<T>()
    private val closed = AtomicBoolean(false)

    private val lock = SpinLock()

    val isEmpty
        get() = values.isEmpty

    val isNotEmpty
        get() = values.isNotEmpty

    val size
        get() = values.size

    override fun push(value: T) {
        val listener = lock.synchronize {
            if (closed.getValue()) {
                throw ClosedException()
            }
            return@synchronize if (listeners.isEmpty) {
                values.push(value)
                null
            } else {
                listeners.pop()
            }
        }
        listener?.resumeWith(Result.success(value))
    }

    override suspend fun pop(): T {
        lock.lock()
        if (closed.getValue()) {
            lock.unlock()
            throw ClosedException()
        }
        if (values.isNotEmpty) {
            val result = values.pop()
            lock.unlock()
            return result
        } else {
            return suspendCancellableCoroutine {
                listeners.push(it)
                lock.unlock()
            }
        }
    }

    init {
        doFreeze()
    }

    val isClosed
        get() = closed.getValue()

    override fun close() {
        lock.synchronize {
            if (closed.getValue()) {
                throw ClosedException()
            }
            closed.setValue(true)
            while (!values.isEmpty) {
                values.pop()
            }
            while (!listeners.isEmpty) {
                listeners.pop().resumeWith(Result.failure(ClosedException()))
            }
        }
    }
}
