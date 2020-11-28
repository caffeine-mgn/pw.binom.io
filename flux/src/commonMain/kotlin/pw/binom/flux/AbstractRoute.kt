package pw.binom.flux

import pw.binom.io.httpServer.Handler

abstract class AbstractRoute : Route {
    private val routers = HashMap<String, ArrayList<RouteImpl>>()
    private val methods = HashMap<String, HashMap<String, ArrayList<suspend (Action) -> Boolean>>>()
    private var forwardHandler: Handler? = null

    override fun route(path: String, func: (Route.() -> Unit)?): Route {
        val r = RouteImpl()
        routers.getOrPut(path) { ArrayList() }.add(r)
        if (func != null)
            func(r)
        return r
    }

    override fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean) {
        if (forwardHandler != null)
            throw IllegalStateException("Router has already defined forward")
        methods.getOrPut(method) { HashMap() }.getOrPut(path) { ArrayList() }.add(func)
    }

    override fun forward(handler: Handler) {
        if (methods.isNotEmpty())
            throw IllegalStateException("Router has endpoint")
        if (forwardHandler != null)
            throw IllegalStateException("Router has already defined forward")
        forwardHandler = handler
    }

    suspend fun execute(action: Action): Boolean {
        val forward = forwardHandler
        if (forward != null) {
            forward.request(action.req, action.resp)
            return true
        }
        routers.entries
                .asSequence()
                .filter {
                    it.key.isWildcardMattech(action.req.contextUri)
                }
                .sortedBy { -it.key.length }
                .flatMap { it.value.asSequence() }
                .forEach {
                    if (it.execute(action))
                        return true
                }
        methods[action.req.method]
                ?.entries
                ?.asSequence()
                ?.filter {
                    action.req.contextUri.isWildcardMattech(it.key)
                }
                ?.sortedBy { -it.key.length }
                ?.flatMap { it.value.asSequence() }
                ?.forEach {
                    if (it(action))
                        return true
                }
        return false
    }
}