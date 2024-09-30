package pw.binom.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.resume

/**
 * Coroutine primitive for wait until [count] will equals or less than [minValue]
 */
class CountWater(count: Int, val minValue: Int = 0) {
  var count: Int = count
    private set
  private val lock = SpinLock()
  private val waters = HashSet<CancellableContinuation<Unit>>()

  fun increment() = lock.synchronize {
    ++count
  }

  fun decrement(): Int {
    lock.lock()
    val newCounterValue = --count
    if (newCounterValue > minValue) {
      lock.unlock()
      return newCounterValue
    }
    lock.unlock()
    waters.forEach {
      it.resume(Unit)
    }
    waters.clear()
    return newCounterValue
  }

  /**
   * Suspends coroutine until [count] will be equals or less than [minValue].
   * If in call moment [count] already equals zero will return immediately
   */
  suspend fun await() {
    lock.lock()
    if (count <= minValue) {
      lock.unlock()
      return
    }
    return suspendCancellableCoroutine { con ->
      con.invokeOnCancellation {
        lock.synchronize {
          waters -= con
        }
      }
      waters += con
      lock.unlock()
    }
  }
}
