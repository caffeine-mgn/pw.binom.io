package pw.binom.coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.synchronize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.jvm.JvmInline

@Suppress("UNCHECKED_CAST")
@JvmInline
value class AwaitFirstResult<T> private constructor(private val value: Any?) {
  companion object {
    fun <T> found(value: T) = AwaitFirstResult<T>(value)
    fun <T> notFound() = AwaitFirstResult<T>(ObjectNotFound)

    private object ObjectNotFound
  }

  val isSuccess
    get() = value !== ObjectNotFound

  val isNotSuccess
    get() = value === ObjectNotFound

  fun getOrThrow(): T {
    if (isNotSuccess) {
      throw NoSuchElementException()
    }
    return value as T
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
val <T> Deferred<T>.asResult: Result<T>
  get() {
    val ex = getCompletionExceptionOrNull()
    return if (ex == null) {
      Result.success(getCompleted())
    } else {
      Result.failure(ex)
    }
  }

suspend fun <T> Collection<Deferred<T>>.awaitFirst(filter: (Result<T>) -> Boolean = { true }): AwaitFirstResult<T> {
  if (isEmpty()) {
    return AwaitFirstResult.notFound()
  }
  val list = toTypedArray()
  return supervisorScope {
    suspendCancellableCoroutine { continuation ->
      val finished = AtomicBoolean(false)
      val count = AtomicInt(size)
      val lock = AtomicBoolean(false)
      list.forEach { async ->
        async.invokeOnCompletion {
          count.dec()
          lock.synchronize {
            if (finished.getValue()) {
              return@synchronize
            }
            val result = async.asResult
            if (!filter(result)) {
              if (count.getValue() == 0) {
                continuation.resume(AwaitFirstResult.notFound())
              }
              return@synchronize
            }
            if (finished.compareAndSet(false, true)) {
              if (result.isSuccess) {
                list.forEach {
                  if (it !== async) {
                    it.cancel()
                  }
                }
                continuation.resume(AwaitFirstResult.found(result.getOrThrow()))
              } else {
                continuation.resumeWithException(result.exceptionOrNull()!!)
              }
            }
          }
        }
      }
    }
  }
}
