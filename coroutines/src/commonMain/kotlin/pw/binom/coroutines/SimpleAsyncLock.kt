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
    private val stateLock = SpinLock()

    override val isLocked: Boolean
        get() = locked.getValue()

    private suspend fun internalLock(lockingTimeout: Duration?) {
        val unlockStatus = stateLock.synchronize { locked.compareAndSet(false, true) }
        if (!unlockStatus) {
            println("SimpleAsyncLock:: Can't lock now")
            withTimeout2(lockingTimeout) {
                suspendCancellableCoroutine {
                    it.invokeOnCancellation { _ ->
                        waiters -= it
                    }
                    println("SimpleAsyncLock:: add to water")
                    waiters += it
                }
            }
        } else {
            println("SimpleAsyncLock:: Locked now!")
        }
    }

    suspend fun lock() = internalLock(null)
    suspend fun lock(lockingTimeout: Duration) = internalLock(lockingTimeout)

    fun unlock() {
        val waiter = stateLock.synchronize {
            val waiter = waiters.firstOrNull()
            if (waiter != null) {
                waiters.remove(waiter)
            }
            println("After unlock: $waiter")
            if (waiter == null) {
                locked.setValue(false)
            }
            waiter
        }
        waiter?.resume(Unit)
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
