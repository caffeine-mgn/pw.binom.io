package pw.binom.flux

import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange

open class CachingHttpHandler(val cachingInput: Boolean, protected val forward: HttpHandler) : HttpHandler {
    override suspend fun handle(exchange: HttpServerExchange) {
        val cached = CachingHttpServerExchange(exchange)
        if (cachingInput) {
            cached.readInput()
        }
        try {
            forward.handle(cached)
        } finally {
            cached.flush()
        }
    }
}
