package pw.binom.flux

import pw.binom.url.PathMask

abstract class AbstractFluxHttpRouting : FluxHttpRouting {
    protected abstract val routingHttpHandler: HttpRouting
    protected abstract val commonFluxSerialization: AbstractCommonFluxSerialization

    override fun route(path: PathMask, handler: FluxHttpHandler) {
        routingHttpHandler.route(
            path = path.toString(),
            handler = { exchange ->
                val fluxExchange = FluxHttpServerExchange(
                    fluxSerialization = commonFluxSerialization,
                    pathMask = path,
                    source = exchange,
                )
                handler.handle(fluxExchange)
            },
        )
    }

    override fun route(method: String, path: PathMask, handler: FluxHttpHandler) {
        routingHttpHandler.route(
            method = method,
            path = path.toString(),
            handler = { exchange ->
                val fluxExchange = FluxHttpServerExchange(
                    fluxSerialization = commonFluxSerialization,
                    pathMask = path,
                    source = exchange,
                )
                handler.handle(fluxExchange)
            },
        )
    }

    override fun get(path: PathMask, handler: FluxHttpHandler) = route(
        method = "GET",
        path = path,
        handler = handler,
    )

    override fun options(path: PathMask, handler: FluxHttpHandler) = route(
        method = "OPTIONS",
        path = path,
        handler = handler,
    )

    override fun post(path: PathMask, handler: FluxHttpHandler) = route(
        method = "POST",
        path = path,
        handler = handler,
    )

    override fun put(path: PathMask, handler: FluxHttpHandler) = route(
        method = "PUT",
        path = path,
        handler = handler,
    )

    override fun delete(path: PathMask, handler: FluxHttpHandler) = route(
        method = "DELETE",
        path = path,
        handler = handler,
    )
}
