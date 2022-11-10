package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.pool.GenericObjectPool

/**
 * Handler for wrap each request to [CachingHttpRequest] and [CachingHttpRequest]
 */
open class CachingHandler(val forward: Handler) : Handler, Closeable {
    private val cachingHttpResponsePool = GenericObjectPool(factory = CachingHttpResponse.FACTORY)
    private val cachingHttpRequestPool =
        GenericObjectPool(factory = CachingHttpRequest.factory(cachingHttpResponsePool))

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
