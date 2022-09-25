package pw.binom.flux

import kotlinx.coroutines.withContext
import pw.binom.collections.defaultHashMap
import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.pool.DefaultPool
import pw.binom.pool.borrow

abstract class AbstractRoute(wrapperPoolCapacity: Int = 16) : Route, Handler {
    private val routers = defaultHashMap<String, ArrayList<Route>>()
    private val methods = defaultHashMap<String, HashMap<String, ArrayList<suspend (FluxHttpRequest) -> Unit>>>()
    private var forwardHandler: Handler? = null

    override fun route(path: String, route: Route) {
        routers.getOrPut(path) { ArrayList() }.add(route)
    }

    override fun route(path: String, func: (Route.() -> Unit)?): Route {
        val r = RouteImpl(serialization)
        routers.getOrPut(path) { ArrayList() }.add(r)
        if (func != null) {
            func(r)
        }
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

    private val requestWrapperPool = DefaultPool<FluxHttpRequestImpl>(wrapperPoolCapacity) { FluxHttpRequestImpl() }

    override suspend fun execute(action: HttpRequest) {
        val forward = forwardHandler
        if (forward != null) {
            forward.request(action)
            return
        }
        if (routers.entries.isNotEmpty()) {
            routers.entries.asSequence().filter {
                action.path.isMatch(it.key)
            }
//                .sortedBy { -it.key.length }
                .flatMap { it.value.asSequence() }.any {
                    it.execute(action)
                    action.response != null
                }
        }
        if (action.response != null) {
            return
        }
        if (methods.isNotEmpty()) {
            methods[action.method]
                ?.entries
                ?.asSequence()
                ?.filter {
                    action.path.isMatch(it.key)
                }
                ?.any { route ->
                    val wrapper = requestWrapperPool.borrow {
                        it.reset(
                            mask = route.key,
                            original = action,
                            serialization = serialization,
                        )
                    }
                    try {
                        if (action.response != null) {
                            return@any true
                        }
                        withContext(wrapper) {
                            route.value.any {
                                it(wrapper)
                                action.response != null
                            }
                        }
                    } finally {
                        wrapper.free()
                        requestWrapperPool.recycle(wrapper)
                    }
                }
        }
    }

    override suspend fun request(req: HttpRequest) {
        execute(req)
    }
}
