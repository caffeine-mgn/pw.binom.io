package pw.binom.io

fun interface AsyncCloseable {
  companion object;

  suspend fun asyncClose()

  suspend fun asyncCloseAnyway() =
    try {
      asyncClose()
      true
    } catch (e: Throwable) {
      false
    }
}

suspend inline fun <T : AsyncCloseable, R> T.useAsync(func: (T) -> R): R {
  val result =
    try {
      func(this)
    } catch (funcException: Throwable) {
      try {
        asyncClose()
      } catch (closeException: Throwable) {
        closeException.addSuppressed(funcException)
        throw closeException
      }
      throw funcException
    }
  asyncClose()
  return result
}
