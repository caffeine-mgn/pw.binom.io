package pw.binom.flux

import pw.binom.io.httpServer.HttpHandler
import pw.binom.url.PathMask

interface HttpRouting {
    fun route(method: String, path: String, handler: HttpHandler)
    fun route(method: String, path: PathMask, handler: HttpHandler) =
        route(
            method = method,
            path = path.toString(),
            handler = handler,
        )

    fun route(path: String, handler: HttpHandler)
    fun route(path: PathMask, handler: HttpHandler) = route(
        path = path.toString(),
        handler = handler,
    )

    fun put(path: String, handler: HttpHandler) =
        route(method = "PUT", path = path, handler = handler)

    fun delete(path: String, handler: HttpHandler) =
        route(method = "DELETE", path = path, handler = handler)

    fun options(path: String, handler: HttpHandler) =
        route(method = "OPTIONS", path = path, handler = handler)

    fun get(path: String, handler: HttpHandler) =
        route(method = "GET", path = path, handler = handler)

    fun post(path: String, handler: HttpHandler) =
        route(method = "POST", path = path, handler = handler)

    fun put(path: PathMask, handler: HttpHandler) = put(path = path.toString(), handler = handler)
    fun delete(path: PathMask, handler: HttpHandler) =
        delete(path = path.toString(), handler = handler)

    fun get(path: PathMask, handler: HttpHandler) = get(path = path.toString(), handler = handler)
    fun post(path: PathMask, handler: HttpHandler) = post(path = path.toString(), handler = handler)
    fun options(path: PathMask, handler: HttpHandler) =
        options(path = path.toString(), handler = handler)
}
