package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.pool.DefaultPool

/**
 * Handler for wrap each request to [CachingHttpRequest] and [CachingHttpRequest]
 */
open class CachingHandler(val forward: Handler, objectPoolSize: Int = 16) : Handler, Closeable {
    private val cachingHttpResponsePool = DefaultPool2<CachingHttpResponse>(capacity = objectPoolSize) { pool ->
        CachingHttpResponse { self -> pool.recycle(self) }
    }
    private val cachingHttpRequestPool =
        DefaultPool<CachingHttpRequest>(capacity = objectPoolSize) { pool ->
            CachingHttpRequest(
                responsePool = cachingHttpResponsePool,
                onClose = { pool.recycle(it) }
            )
        }

    private class DefaultPool2<T : Any>(capacity: Int, new: (DefaultPool<T>) -> T) :
        DefaultPool<T>(capacity = capacity, new = new)

    override suspend fun request(req: HttpRequest) {
        val cachingRequest = cachingHttpRequestPool.borrow()
            .also {
                it.reset(req)
            }
        try {
            forward.request(cachingRequest)
        } finally {
            cachingRequest.finish()
//            cachingRequest.response?.sendAndClose()
        }
    }

    override fun close() {
    }
}
