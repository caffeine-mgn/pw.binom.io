package pw.binom.coroutine

interface CrossThreadContinuation<T> {
    fun resumeWith(result: Result<T>)
    fun resume(value: T) = resumeWith(Result.success(value))
    fun resumeWithException(exception: Throwable) = resumeWith(Result.failure(exception))
}
