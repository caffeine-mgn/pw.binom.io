package pw.binom.coroutine

interface CrossThreadContinuation<T> {
    fun coroutine(result: Result<T>)
}