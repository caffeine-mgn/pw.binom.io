package pw.binom.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.io.ClosedException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("UNCHECKED_CAST")
class AsyncExchange<T> : AsyncCloseable {
  private var valueSet = false
  private var value: T? = null
  private var closed = AtomicBoolean(false)
  private val valueLock = SpinLock()
  private val getWaiters = ArrayList<Continuation<T>>()
  private val setWaiters = ArrayList<Continuation<Unit>>()
  suspend fun extract(): T {
    if (closed.getValue()) {
      throw ClosedException()
    }
    valueLock.lock()
    if (valueSet) {
      val v = value
      this.value = null
      valueSet = false
      val water = setWaiters.removeLastOrNull()
      if (water == null) {
        valueLock.unlock()
      } else {
        water.resume(Unit)
      }
      return v as T
    } else {
      return suspendCancellableCoroutine { con ->
        getWaiters += con
        valueLock.unlock()
      }
    }
  }

  suspend fun push(value: T) {
    if (closed.getValue()) {
      throw ClosedException()
    }
    valueLock.lock()
    if (valueSet) {
      suspendCancellableCoroutine { con ->
        setWaiters += con
        valueLock.unlock()
      }
    }
    val r = getWaiters.removeLastOrNull()
    if (r == null) {
      valueSet = true
      this.value = value
      valueLock.unlock()
    } else {
      valueLock.unlock()
      r.resume(value)
    }
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    valueLock.synchronize {
      getWaiters.forEach {
        it.resumeWithException(ClosedException())
      }
      setWaiters.forEach {
        it.resumeWithException(ClosedException())
      }
      getWaiters.clear()
      setWaiters.clear()
    }
  }
}
