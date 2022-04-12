package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.pool.DefaultPool

/**
 * Handler for wrap each request to [CachingResponseHttpRequest] and [CachingResponseHttpRequest]
 */
class CachingHandler(val forward: Handler, objectPoolSize: Int = 16) : Handler, Closeable {
    private val cachingHttpHttpResponsePool = DefaultPool2<CachingHttpHttpResponse>(capacity = objectPoolSize) { pool ->
        CachingHttpHttpResponse { self -> pool.recycle(self) }
    }
    private val cachingResponseHttpRequestPool =
        DefaultPool<CachingResponseHttpRequest>(capacity = objectPoolSize) { CachingResponseHttpRequest(responsePool = cachingHttpHttpResponsePool) }

    private class DefaultPool2<T : Any>(capacity: Int, new: (DefaultPool<T>) -> T) :
        DefaultPool<T>(capacity = capacity, new = new)

    override suspend fun request(req: HttpRequest) {
        val cachingRequest = cachingResponseHttpRequestPool.borrow {
            it.original = req
        }
        try {
            forward.request(cachingRequest)
        } finally {
            cachingRequest.response?.sendAndClose()
            cachingResponseHttpRequestPool.recycle(cachingRequest)
        }
    }

    override fun close() {
    }
}
