package pw.binom.flux

abstract class AbstractRoute : Route {
    private val routers = HashMap<String, ArrayList<RouteImpl>>()
    private val methods = HashMap<String, HashMap<String, ArrayList<suspend (Action) -> Boolean>>>()

    override fun route(path: String): Route {
        val r = RouteImpl()
        routers.getOrPut(path) { ArrayList() }.add(r)
        return r
    }

    override fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean) {
        methods.getOrPut(method) { HashMap() }.getOrPut(path) { ArrayList() }.add(func)
    }

    suspend fun execute(action: Action): Boolean {
        val endpoints = methods[action.req.method]
                ?.entries
                ?.asSequence()
                ?.filter {
                    action.req.contextUri.isWilcardMattech(it.key)
                }
                ?.sortedBy { it.key.length }
                ?.flatMap { it.value.asSequence() }
        if (endpoints != null) {
            for (e in endpoints) {
                if (e(action))
                    return true
            }
        }

        routers.entries
                .asSequence()
                .filter {
                    it.key.isWilcardMattech(action.req.contextUri)
                }
                .sortedBy { it.key.length }
                .flatMap { it.value.asSequence() }
                .forEach {
                    if (it.execute(action))
                        return true
                }
        return false
    }
}