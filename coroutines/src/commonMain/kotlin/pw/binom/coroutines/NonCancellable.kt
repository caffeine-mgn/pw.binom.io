package pw.binom.coroutines

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.startCoroutine

/**
 * Запускает не отменяемую корутину [func].
 * Если во время выполнения корутины случилась отмена, то вызовется функция [onCancel].
 *
 * @param ctx контекст новой корутины [func]. По умолчанию текущий контекст
 * @param onCancel вызывается при отмене основной корутины. Если кидает исключение, то
 * оно заменит собою исключение отмены
 * @param func запускаемая корутина
 * @return результат работы [func]
 */
inline suspend fun <T> nonCancellable(
  ctx: CoroutineContext? = null,
  onCancel: (Throwable) -> T = { ex -> throw ex },
  noinline func: suspend () -> T,
): T {
  var canceledException: Throwable? = null
  return try {
    suspendCancellableCoroutine<T> { cancellable ->
      cancellable.invokeOnCancellation { er ->
        canceledException = er ?: CancellationException()
      }
      val ctx = ctx ?: cancellable.context.minusKey(Job.Key)
      func.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext
          get() = ctx

        override fun resumeWith(result: Result<T>) {
          cancellable.resumeWith(result)
        }
      })
    }
  } catch (e: CancellationException) {
    val canceledException = canceledException
    if (canceledException != null) {
      onCancel(canceledException)
    } else {
      throw e
    }
  }
}
