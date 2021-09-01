package pw.binom.coroutine

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.FrozenQueue
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.suspendManagedCoroutine
import pw.binom.concurrency.synchronize
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException

class AsyncQueue<T> : Closeable, AsyncExchangeInput<T>, ExchangeOutput<T> {
    private val listeners = FrozenQueue<CrossThreadContinuation<T>>()
    private val values = FrozenQueue<T>()
    private val closed = AtomicBoolean(false)

    private val lock = SpinLock()

    override fun push(value: T) {
        val listener = lock.synchronize {
            if (closed.value) {
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
        var found = false
        var result: T? = null
        lock.synchronize {
            if (closed.value) {
                throw ClosedException()
            }
            if (!values.isEmpty) {
                found = true
                result = values.pop()
            }
        }
        if (found) {
            return result as T
        }
        return suspendManagedCoroutine {
            listeners.push(it)
        }
    }

    init {
        doFreeze()
    }

    override fun close() {
        lock.synchronize {
            if (closed.value) {
                throw ClosedException()
            }
            closed.value = true
            while (!values.isEmpty) {
                values.pop()
            }
            while (!listeners.isEmpty) {
                listeners.pop().resumeWith(Result.failure(ClosedException()))
            }
        }

    }
}