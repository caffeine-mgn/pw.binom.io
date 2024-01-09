@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.coroutines

import kotlinx.coroutines.*
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.coroutines.coroutineContext

class AsyncLazy<T>(val scope: CoroutineScope = GlobalScope, private val func: suspend () -> T) {
  /*
  companion object {
    private object InProcess

    private object Canceled

    private object NotReady
  }

  private var result: Any? = NotReady
  private var isError = false
  private val lock = SpinLock()
  private var waters = ArrayList<CancellableContinuation<T>>()

  fun reset() {
    lock.synchronize {
      when {
        result === NotReady || result === Canceled -> {
          // Do nothing
        }

        result === InProcess -> {
          lock.synchronize {
            result = Canceled
          }
        }
      }
    }
  }

  val isInProgress
    get() = lock.synchronize { result === InProcess }
  val isInitialized
    get() = lock.synchronize { result !== NotReady && result !== InProcess }
*/
  private val lock2 = SpinLock()
  private var job: Deferred<T>? = null

  fun reset() {
    lock2.synchronize {
      job?.cancel()
      job = null
    }
  }

  suspend fun get(): T {
    lock2.lock()
    var job = job
    if (job == null) {
      job =
        scope.async(coroutineContext, start = CoroutineStart.LAZY) {
          func()
        }
      this.job = job
      lock2.unlock()
      job.start()
    } else {
      lock2.unlock()
    }

    return job.await()
  }
  /*
    @Suppress("UNCHECKED_CAST")
    suspend fun get(): T {
      lock.lock()
      when {
        result === NotReady -> {
          result = InProcess
          lock.unlock()
          val r =
            try {
              func()
            } catch (e: Throwable) {
              val waters =
                lock.synchronize {
                  if (result === Canceled) {
                    val exp = CancellationException(null as String?)
                    waters.forEach {
                      it.resumeWithException(exp)
                    }
                    throw CancellationException(null as String?)
                  }
                  isError = true
                  result = e
                  val waters = waters
                  this.waters = ArrayList()
                  waters
                }
              waters.forEach {
                it.resumeWithException(e)
              }
              throw e
            }
          val waters =
            lock.synchronize {
              if (result === Canceled) {
                val exp = CancellationException(null as String?)
                waters.forEach {
                  it.resumeWithException(exp)
                }
                throw exp
              }
              result = r
              val waters = waters
              this.waters = ArrayList()
              waters
            }
          waters.forEach {
            it.resume(r)
          }
          return r
        }

        result === InProcess -> {
          return suspendCancellableCoroutine { con ->
            waters += con
            lock.unlock()
          }
        }

        else -> {
          val isError = isError
          val result = result
          lock.unlock()
          if (isError) {
            throw result as Throwable
          } else {
            return result as T
          }
        }
      }
    }
   */
}

fun <FROM, TO> AsyncLazy<FROM>.map(
  scope: CoroutineScope = GlobalScope,
  func: suspend (FROM) -> TO,
) = asyncLazyOf(scope = scope) { func(this.get()) }

fun <T> asyncLazyOf(
  scope: CoroutineScope = GlobalScope,
  func: suspend () -> T,
) = AsyncLazy(scope = scope, func = func)
