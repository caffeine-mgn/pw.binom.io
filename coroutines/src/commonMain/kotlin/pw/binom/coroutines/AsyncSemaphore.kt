package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicInt
import pw.binom.collections.LinkedList
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume

class AsyncSemaphore(val counter: Int) {
  private val actualCounter = AtomicInt(0)
  private val lock = SpinLock()
  private val waiters = LinkedList<CancellableContinuation<Unit>>()
  private fun resumeWater() {
    lock.synchronize {
      waiters.removeLastOrNull()
    }?.resume(Unit)
  }

  suspend fun lock() {
    val needAwait = lock.synchronize {
      actualCounter.getValue() < counter
    }

    if (needAwait) {
      suspendCancellableCoroutine { con ->
        lock.synchronize {
          waiters.addLast(con)
        }
      }
    }
    lock.synchronize {
      actualCounter.inc()
    }
  }

  fun unlock() {
    actualCounter.dec()
    resumeWater()
  }

  suspend inline fun <T> synchronize(func: () -> T): T {
    lock()
    try {
      return func()
    } finally {
      unlock()
    }
  }
}
