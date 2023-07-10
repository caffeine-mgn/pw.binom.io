package pw.binom.io.httpServer

interface RouteHttpHandler : HttpHandler {
    fun addRoute(method: String, path: String, route: () -> HttpHandler)
    fun addRoute(method: String, path: String, route: HttpHandler) =
        addRoute(
            method = method,
            path = path,
            route = { route },
        )
}
