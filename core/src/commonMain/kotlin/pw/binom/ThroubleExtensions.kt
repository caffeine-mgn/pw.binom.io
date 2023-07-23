package pw.binom

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalContracts::class)
inline fun Continuation<*>.resumeOnException(afterCall: (Throwable?) -> Unit = {}, func: () -> Unit): Boolean {
  contract {
    callsInPlace(afterCall, InvocationKind.EXACTLY_ONCE)
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    func()
    afterCall(null)
    false
  } catch (e: Throwable) {
    resumeWithException(e)
    afterCall(e)
    true
  }
}

/**
 * Executes [func] and then call [resumeWithException] with [exception]. If [func] throws exception will add new
 * exception to [exception] using [Throwable.addSuppressed] and after that call [resumeWithException] with [exception]
 */
inline fun Continuation<*>.executeAndResumeWithException(exception: Throwable, func: () -> Unit) {
  try {
    func()
    resumeWithException(exception)
  } catch (e: Throwable) {
    exception.addSuppressed(e)
    resumeWithException(exception)
  }
  return
}
