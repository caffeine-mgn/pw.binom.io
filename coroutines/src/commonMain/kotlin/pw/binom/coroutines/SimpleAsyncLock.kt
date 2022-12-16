package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume
import kotlin.time.Duration

class SimpleAsyncLock : AsyncLock {
    private val waiters by lazy {
        defaultMutableSet<CancellableContinuation<Unit>>()
    }
    private val locked = AtomicBoolean(false)
    private val waiterLock = SpinLock()

    override val isLocked: Boolean
        get() = locked.getValue()

    private suspend fun internalLock(lockingTimeout: Duration?) {
        if (!locked.compareAndSet(false, true)) {
            withTimeout2(lockingTimeout) {
                suspendCancellableCoroutine {
                    it.invokeOnCancellation { _ ->
                        waiters -= it
                    }
                    waiterLock.synchronize {
                        waiters += it
                    }
                }
            }
        }
    }

    suspend fun lock() = internalLock(null)
    suspend fun lock(lockingTimeout: Duration) = internalLock(lockingTimeout)

    fun unlock() {
        val waiter = waiterLock.synchronize {
            val waiter = waiters.firstOrNull()
            if (waiter != null) {
                waiters.remove(waiter)
            }
            waiter
        }
        if (waiter == null) {
            locked.setValue(false)
        } else {
            waiter.resume(Unit)
        }
    }

    override suspend fun <T> synchronize(lockingTimeout: Duration, func: suspend () -> T): T {
        internalLock(lockingTimeout)
        return try {
            func()
        } finally {
            unlock()
        }
    }

    override suspend fun <T> synchronize(func: suspend () -> T): T {
        internalLock(null)
        return try {
            func()
        } finally {
            unlock()
        }
    }
}
