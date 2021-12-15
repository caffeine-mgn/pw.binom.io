package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

abstract class AbstractRoute : Route, Handler {
    private val routers = HashMap<String, ArrayList<Route>>()
    private val methods = HashMap<String, HashMap<String, ArrayList<suspend (FluxHttpRequest) -> Unit>>>()
    private var forwardHandler: Handler? = null

    override fun route(path: String, route: Route) {
        routers.getOrPut(path) { ArrayList() }.add(route)
    }

    override fun route(path: String, func: (Route.() -> Unit)?): Route {
        val r = RouteImpl(serialization)
        routers.getOrPut(path) { ArrayList() }.add(r)
        if (func != null)
            func(r)
        return r
    }

    override fun detach(path: String, route: Route) {
        val list = routers[path] ?: return
        if (list.remove(route) && list.isEmpty()) {
            routers.remove(path)
        }
    }

    override fun endpoint(method: String, path: String, func: suspend (FluxHttpRequest) -> Unit): Closeable {
        if (forwardHandler != null) {
            throw IllegalStateException("Router has already defined forward")
        }
        methods.getOrPut(method) { HashMap() }.getOrPut(path) { ArrayList() }.add(func)
        return Closeable {
            methods[method]?.get(path)?.remove(func)
            if (methods[method]?.get(path)?.isEmpty() == true) {
                methods[method]?.remove(path)
            }
            if (methods[method]?.isEmpty() == true) {
                methods.remove(method)
            }
        }
    }

    override fun forward(handler: Handler?) {
        if (methods.isNotEmpty()) {
            throw IllegalStateException("Router has endpoint")
        }
        if (handler != null && forwardHandler != null) {
            throw IllegalStateException("Router has already defined forward")
        }
        forwardHandler = handler
    }

    override suspend fun execute(request: HttpRequest) {
        println("${this::class} -> execute #1 forward=$forwardHandler")
        val forward = forwardHandler
        if (forward != null) {
            forward.request(request)
            return
        }
        println("${this::class} -> execute #2 routers.size=${routers.size}, routers.keys=${routers.keys}")
        if (routers.entries.isNotEmpty()) {
            routers.entries
                .asSequence()
                .filter {
                    request.path.isMatch(it.key)
                }
//                .sortedBy { -it.key.length }
                .flatMap { it.value.asSequence() }
                .forEach {
                    it.execute(request)
                    if (request.response != null) {
                        return
                    }
                }
        }
        println("${this::class} -> execute #3 methods.size=${methods.size}, methods.keys=${methods.keys}, methods[${request.method}]?.keys=${methods[request.method]?.keys}")
        if (methods.isNotEmpty()) {
            methods[request.method]
                ?.entries
                ?.asSequence()
                ?.filter {
                    request.path.isMatch(it.key)
                }
                ?.forEach { route ->
                    route.value.forEach {
                        it(FluxHttpRequestImpl(mask = route.key, serialization = serialization, request = request))
                        if (request.response != null) {
                            return
                        }
                    }
                }
        }
    }

    override suspend fun request(req: HttpRequest) {
        execute(req)
    }
}