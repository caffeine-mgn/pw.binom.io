package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

/**
 * Handler for wrap each request to [CachingResponseHttpRequest] and [CachingResponseHttpRequest]
 */
class CachingHandler(val forward: Handler) : Handler {
    override suspend fun request(req: HttpRequest) {
        val cachingRequest = CachingResponseHttpRequest(req)
        try {
            forward.request(cachingRequest)
        } finally {
            cachingRequest.response?.sendAndClose()
        }
    }
}
