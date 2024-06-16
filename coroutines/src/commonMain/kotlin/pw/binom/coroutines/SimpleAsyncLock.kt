package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

class SimpleAsyncLock : AsyncLock {
  internal val waiters by lazy {
    defaultMutableSet<CancellableContinuation<Unit>>()
  }
  private val locked = AtomicBoolean(false)
  private val stateLock = SpinLock()
  private val waiterLock = SpinLock()

  override val isLocked: Boolean
    get() = locked.getValue()

  private suspend fun internalLock(lockingTimeout: Duration?) {
    val unlockStatus = stateLock.synchronize { locked.compareAndSet(false, true) }
    if (!unlockStatus) {
      withTimeout2(lockingTimeout) {
        suspendCancellableCoroutine {
          it.invokeOnCancellation { _ ->
            waiterLock.synchronize {
              waiters -= it
            }
          }
          waiterLock.synchronize {
            waiters += it
          }
        }
      }
    }
  }

  fun tryLock(): Boolean = stateLock.synchronize { locked.compareAndSet(false, true) }

  suspend fun lock() = internalLock(null)

  suspend fun lock(lockingTimeout: Duration) = internalLock(lockingTimeout)

  fun unlock() {
    val waiter =
      stateLock.synchronize {
        val waiter = waiterLock.synchronize {
          val waiter = waiters.firstOrNull()
          if (waiter != null) {
            waiters -= waiter
          }
          waiter
        }
        if (waiter == null) {
          locked.setValue(false)
        }
        waiter
      }
    waiter?.resume(Unit)
  }

  override suspend fun <T> synchronize(
    lockingTimeout: Duration,
    func: suspend () -> T,
  ): T {
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

  override suspend fun <T> trySynchronize(
    lockingTimeout: Duration,
    func: suspend () -> T,
  ): AsyncLock.SynchronizeResult<T> {
    if (!tryLock()) {
      return AsyncLock.SynchronizeResult.notLocked()
    }
    return try {
      AsyncLock.SynchronizeResult.locked(func())
    } finally {
      unlock()
    }
  }

  override fun throwAll(e: Throwable) = waiterLock.synchronize {
    val size = waiters.size
    waiters.forEach {
      it.resumeWithException(e)
    }
    waiters.clear()
    size
  }
}
