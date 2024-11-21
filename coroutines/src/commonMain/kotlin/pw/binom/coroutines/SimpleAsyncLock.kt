package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import pw.binom.collections.InternalApi
import pw.binom.collections.LinkedList
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

class SimpleAsyncLock : AsyncLock {
  private val waters = LinkedList<CancellableContinuation<Unit>>()
  override val isLocked: Boolean
    get() = state.synchronize { locked }

  private val state = SpinLock()
  private var locked = false


  fun tryLock() = state.synchronize {
    if (!locked) {
      locked = true
      true
    } else {
      false
    }
  }

  @OptIn(InternalApi::class)
  suspend fun lock() {
    state.lock()
    if (locked) {
      suspendCancellableCoroutine {
        val node = waters.addLast(it)
        state.unlock()
        it.invokeOnCancellation {
          state.synchronize {
            waters.unlink(node)
          }
        }
      }
    } else {
      locked = true
      state.unlock()
    }
  }

  fun unlock() {
    state.synchronize {
      val con = waters.removeFirstOrNull()
      if (con == null) {
        locked = false
      }
      con
    }?.resume(Unit)
  }


  override suspend fun <T> synchronize(lockingTimeout: Duration, func: suspend () -> T): T {
    if (lockingTimeout.isInfinite()) {
      lock()
    } else {
      withTimeout(lockingTimeout) {
        lock()
      }
    }
    return try {
      func()
    } finally {
      unlock()
    }
  }

  override fun throwAll(e: Throwable): Int {
    val list = state.synchronize {
      val newList = ArrayList(waters)
      waters.clear()
      newList
    }
    list.forEach {
      it.resumeWithException(e)
    }
    return list.size
  }

  override suspend fun <T> trySynchronize(
    lockingTimeout: Duration,
    func: suspend () -> T,
  ): AsyncLock.SynchronizeResult<T> {
    if (lockingTimeout.isInfinite()) {
      lock()
      return AsyncLock.SynchronizeResult.locked(func())
    }
    val locked = withTimeoutOrNull(lockingTimeout) {
      lock()
    } != null

    if (!locked) {
      return AsyncLock.SynchronizeResult.notLocked()
    }
    return AsyncLock.SynchronizeResult.locked(func())
  }

}


/*
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
*/
