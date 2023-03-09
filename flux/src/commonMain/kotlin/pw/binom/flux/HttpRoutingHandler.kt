package pw.binom.flux

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.toPathMask

open class HttpRoutingHandler : HttpRouting, HttpHandler {
    private val methods = defaultMutableMap<String, MutableMap<String, MutableList<HttpHandler>>>()
    private val paths = defaultMutableMap<String, MutableList<HttpHandler>>()

    override fun route(method: String, path: String, handler: HttpHandler) {
        val methodMap = methods.getOrPut(method) { defaultMutableMap() }
        methodMap.getOrPut(path) { defaultMutableList() }.add(handler)
    }

    override fun route(path: String, handler: HttpHandler) {
        paths.getOrPut(path) { defaultMutableList() }.add(handler)
    }

    override suspend fun handle(exchange: HttpServerExchange) {
        val requestPath = exchange.requestURI.path
        paths[requestPath.toString()]?.forEach {
            it.handle(exchange)
            if (exchange.responseStarted) {
                return
            }
        }
        paths.forEach { (path, handlers) ->
            if (requestPath.isMatch(path.toPathMask())) {
                handlers.forEach {
                    it.handle(exchange)
                    if (exchange.responseStarted) {
                        return
                    }
                }
            }
        }
        val paths = methods[exchange.requestMethod] ?: return
        paths[requestPath.toString()]?.forEach {
            it.handle(exchange)
            if (exchange.responseStarted) {
                return
            }
        }
        paths.forEach { (path, handlers) ->
            if (requestPath.isMatch(path)) {
                handlers.forEach {
                    it.handle(exchange)
                    if (exchange.responseStarted) {
                        return
                    }
                }
            }
        }
    }
}
