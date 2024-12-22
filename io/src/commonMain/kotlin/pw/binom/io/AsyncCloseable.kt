package pw.binom.io

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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

@OptIn(ExperimentalContracts::class)
suspend inline fun <T : AsyncCloseable, R> T.useAsync(func: (T) -> R): R {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  var exception: Throwable? = null
  return try {
    return func(this)
  } catch (e: Throwable) {
    exception = e
    throw e
  } finally {
    this.closeFinally(exception)
  }
}

@PublishedApi
internal suspend fun AsyncCloseable?.closeFinally(cause: Throwable?): Unit = when {
  this == null -> {}
  cause == null -> asyncClose()
  else ->
    try {
      asyncClose()
    } catch (closeException: Throwable) {
      cause.addSuppressed(closeException)
    }
}
