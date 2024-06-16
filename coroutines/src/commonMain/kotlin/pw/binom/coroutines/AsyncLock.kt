package pw.binom.coroutines

import kotlin.jvm.JvmInline
import kotlin.time.Duration

interface AsyncLock {

  @JvmInline
  value class SynchronizeResult<T> private constructor(private val raw: Any?) {
    companion object {
      fun <T> notLocked() = SynchronizeResult<T>(FAILD)
      fun <T> locked(value: T) = SynchronizeResult<T>(value)
    }

    private object FAILD

    val isLocked
      get() = raw !== FAILD

    val isNotLocked
      get() = raw === FAILD

    @Suppress("UNCHECKED_CAST")
    fun getOrThrow(): T {
      if (isNotLocked) {
        throw IllegalStateException("Lock was not success locked")
      }
      return raw as T
    }
  }

  val isLocked: Boolean
  suspend fun <T> synchronize(lockingTimeout: Duration, func: suspend () -> T): T
  suspend fun <T> trySynchronize(
    lockingTimeout: Duration = Duration.INFINITE,
    func: suspend () -> T,
  ): SynchronizeResult<T>

  suspend fun <T> synchronize(func: suspend () -> T): T =
    synchronize(
      lockingTimeout = Duration.INFINITE,
      func = func,
    )

  /**
   * Resumes all waiting coroutines with [exception]
   */
  fun throwAll(e: Throwable): Int
}
