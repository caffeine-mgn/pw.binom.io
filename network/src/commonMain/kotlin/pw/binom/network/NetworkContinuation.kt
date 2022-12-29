package pw.binom.network

/*
class NetworkContinuation<T>(
    override val context: CoroutineContext,
    val original: Continuation<T>,
) : Continuation<T> {
    override fun resumeWith(result: Result<T>) {
        original.resumeWith(result)
    }
}

// @Suppress("WRONG_MODIFIER_TARGET")
fun CoroutineContext.getNetworkInterceptor(): NetworkInterceptor? = this[NetworkInterceptor.Key]

suspend fun <T> suspendNetworkCoroutine(networkInterceptor: NetworkInterceptor, c: (NetworkContinuation<T>) -> Unit) {
    suspendCoroutine<T> { continuation ->
//        val interceptor = continuation.context.getNetworkInterceptor()?:networkInterceptor
//        continuation.context[NetworkInterceptor.Key]
        continuation as NetworkContinuation
        c(continuation)
    }
}

private fun <T> (suspend () -> T).start(networkInterceptor: NetworkInterceptor) {
    val continuation = NetworkContinuation<T>(
        context = EmptyCoroutineContext,
        networkInterceptor = networkInterceptor
    )
    if (networkInterceptor.isDispatchNeeded(continuation.context)) {
        networkInterceptor.dispatch(continuation.context) {
            this.startCoroutine(continuation)
        }
    } else {
        this.startCoroutine(continuation)
    }
}
*/
