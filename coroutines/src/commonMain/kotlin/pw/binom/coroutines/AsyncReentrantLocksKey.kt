package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.jvm.JvmInline
import kotlin.time.Duration

private object AsyncReentrantLocksKey : CoroutineContext.Key<AsyncReentrantLocksElement>
private class AsyncReentrantLocksElement : CoroutineContext.Element {
  override val key: CoroutineContext.Key<*>
    get() = AsyncReentrantLocksKey
  private val locks = defaultMutableSet<AsyncReentrantLock>()
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

@OptIn(ExperimentalContracts::class)
internal suspend fun <T> withTimeout2(timeout: Duration?, block: suspend () -> T): T {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }

  return if (timeout == null || timeout == Duration.INFINITE) {
    block()
  } else {
    withTimeout(timeout) { block() }
  }
}

class AsyncReentrantLock : AsyncLock {
  private val waiters by lazy {
    defaultMutableSet<CancellableContinuation<Unit>>()
  }
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
      locked.setValue(false)
    } else {
      waiter.resume(Unit)
    }
  }

  /**
   * Resumes all waiting coroutines with [exception]
   */
  fun resumeAllWithException(exception: Throwable) = waiterLock.synchronize {
    waiters.forEach {
      it.resumeWithException(exception)
    }
    val size = waiters.size
    waiters.clear()
    size
  }

  override val isLocked: Boolean
    get() = locked.getValue()

  override suspend fun <T> synchronize(func: suspend () -> T): T = synchronize(
    lockingTimeout = Duration.INFINITE,
    func = func,
  )

  override suspend fun <T> trySynchronize(
    lockingTimeout: Duration,
    func: suspend () -> T,
  ): AsyncLock.SynchronizeResult<T> =
    trySynchronize(
      lockingTimeout = lockingTimeout,
      waitLock = false,
      func = func
    )

  override suspend fun <T> synchronize(lockingTimeout: Duration, func: suspend () -> T): T =
    trySynchronize(
      lockingTimeout = lockingTimeout,
      waitLock = true,
      func = func
    ).getOrThrow()

  private suspend fun <T> trySynchronize(
    lockingTimeout: Duration,
    waitLock: Boolean,
    func: suspend () -> T,
  ): AsyncLock.SynchronizeResult<T> {
    val locks = coroutineContext[AsyncReentrantLocksKey]
    val isCurrentLockActive = locks?.isExist(this) == true // is this lock already locked in this coroutine
    if (isCurrentLockActive) {
      return AsyncLock.SynchronizeResult.locked(func())
    }
    // Try lock
    if (!locked.compareAndSet(expected = false, new = true)) {
      if (!waitLock) {
        return AsyncLock.SynchronizeResult.notLocked()
      }
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
          withTimeout2(lockingTimeout) {
            AsyncLock.SynchronizeResult.locked(func())
          }
        }
      } else {
        locks.add(this)
        try {
          withTimeout2(lockingTimeout) {
            AsyncLock.SynchronizeResult.locked(func())
          }
        } finally {
          locks.remove(this)
        }
      }
    } finally {
      releaseLock()
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
