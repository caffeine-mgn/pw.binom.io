package pw.binom.concurrency

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pw.binom.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

private object AsyncReentrantLocksKey : CoroutineContext.Key<AsyncReentrantLocksElement>
private class AsyncReentrantLocksElement : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = AsyncReentrantLocksKey
    private val locks = HashSet<AsyncReentrantLock>()
    private val locksLock = SpinLock()
    fun add(lock: AsyncReentrantLock) {
        locksLock.synchronize {
            locks += lock
        }
    }

    fun remove(lock: AsyncReentrantLock) {
        locksLock.synchronize {
            locks -= lock
        }
    }

    fun isExist(lock: AsyncReentrantLock) =
        locksLock.synchronize {
            lock in locks
        }
}

class AsyncReentrantLock : AsyncLock {
    private val waiters = HashSet<CancellableContinuation<Unit>>()
    private val waiterLock = SpinLock()
    private val locked = AtomicBoolean(false)
    private fun releaseLock() {
        val waiter = waiterLock.synchronize {
            val waiter = waiters.firstOrNull()
            if (waiter != null) {
                waiters.remove(waiter)
            }
            waiter
        }
        if (waiter == null) {
            locked.value = false
        } else {
            waiter.resume(Unit)
        }
    }

    override val isLocked: Boolean
        get() = locked.value

    override suspend fun <T> synchronize(func: suspend () -> T): T {
        val locks = coroutineContext[AsyncReentrantLocksKey]
        val isCurrentLockActive = locks?.isExist(this) == true // is this lock already locked in this coroutine
        if (isCurrentLockActive) {
            return func()
        }
        // Try lock
        if (!locked.compareAndSet(expected = false, new = true)) {
            // if lock failed wait until is free
            suspendCancellableCoroutine<Unit> {
                waiterLock.synchronize {
                    waiters += it
                }
                it.invokeOnCancellation { _ ->
                    waiterLock.synchronize {
                        waiters -= it
                    }
                }
            }
        }
        try {
            return if (locks == null) {
                val newLocks = AsyncReentrantLocksElement()
                newLocks.add(this)
                withContext(coroutineContext + newLocks) {
                    func()
                }
            } else {
                locks.add(this)
                try {
                    func()
                } finally {
                    locks.remove(this)
                }
            }
        } finally {
            releaseLock()
        }
    }
}
