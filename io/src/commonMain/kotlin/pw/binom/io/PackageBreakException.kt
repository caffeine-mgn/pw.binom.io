package pw.binom.io

import kotlin.coroutines.cancellation.CancellationException

/**
 * Кидается в случае когда не удаётся прочитать часть необходимых данных,
 * тем самым повреждая поток данных
 */
open class PackageBreakException : IOException {
  constructor()
  constructor(message: String?)
  constructor(cause: Throwable?)
  constructor(message: String?, cause: Throwable?)

  companion object {
    /**
     * Вызывает [func]. В случае возникновения исключения кидает исключение [PackageBreakException]
     */
    inline fun <T> throwOnException(msg: String? = null, func: () -> T) = try {
      func()
    } catch (e: Throwable) {
      throw PackageBreakException(message = msg, cause = e)
    }
  }
}

