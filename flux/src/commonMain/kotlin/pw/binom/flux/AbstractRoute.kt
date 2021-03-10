package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.*

abstract class AbstractRoute : Route, Handler {
    private val routers = HashMap<String, ArrayList<Route>>()
    private val methods = HashMap<String, HashMap<String, ArrayList<suspend (HttpRequest) -> Boolean>>>()
    private var forwardHandler: Handler? = null

    override fun route(path: String, route: Route) {
        routers.getOrPut(path) { ArrayList() }.add(route)
    }

    override fun route(path: String, func: (Route.() -> Unit)?): Route {
        val r = RouteImpl()
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

    override fun endpoint(method: String, path: String, func: suspend (HttpRequest) -> Boolean): Closeable {
        if (forwardHandler != null)
            throw IllegalStateException("Router has already defined forward")
        methods.getOrPut(method) { HashMap() }.getOrPut(path) { ArrayList() }.add(func)
        return Closeable {
            methods[method]?.remove(func)
            if (methods[method]?.isEmpty() == true) {
                methods.remove(method)
            }
        }
    }

    override fun forward(handler: Handler?) {
        if (methods.isNotEmpty())
            throw IllegalStateException("Router has endpoint")
        if (handler != null && forwardHandler != null)
            throw IllegalStateException("Router has already defined forward")
        forwardHandler = handler
    }

    override suspend fun execute(action: HttpRequest): Boolean {
        val forward = forwardHandler
        if (forward != null) {
            forward.request(action)
            return true
        }
        routers.entries
            .asSequence()
            .filter {
                action.urn.isMatch(it.key)
            }
            .sortedBy { -it.key.length }
            .flatMap { it.value.asSequence() }
            .forEach {
                if (it.execute(action))
                    return true
            }
        methods[action.method]
            ?.entries
            ?.asSequence()
            ?.filter {
                action.urn.isMatch(it.key)
            }
            ?.sortedBy { -it.key.length }
            ?.flatMap { it.value.asSequence() }
            ?.forEach {
                if (it(action))
                    return true
            }
        return false
    }

    override suspend fun request(req: HttpRequest) {
        execute(req)
    }
}