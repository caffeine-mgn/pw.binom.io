package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

interface Route {
    fun route(path: String, route: Route)
    fun route(path: String, func: (Route.() -> Unit)? = null): Route
    fun detach(path: String,route:Route)
    fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean):Closeable
    fun forward(handler: Handler?)
    suspend fun execute(action: Action): Boolean
}

inline fun Route.get(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("GET", path, func)
inline fun Route.head(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("HEAD", path, func)
inline fun Route.patch(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("PATCH", path, func)
inline fun Route.trace(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("TRACE", path, func)
inline fun Route.options(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("OPTIONS", path, func)
inline fun Route.connect(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("CONNECT", path, func)
inline fun Route.post(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("POST", path, func)
inline fun Route.put(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("PUT", path, func)
inline fun Route.delete(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("DELETE", path, func)

/**
 * Called before request
 *
 * Example:
 * ```
 * //Check authorized request
 * router.preHandle{ action ->
 *     if (action.basicAuth == null){
 *          action.resp.requestBasicAuth()
 *          return false
 *     }
 *     true
 * }
 * ```
 *
 * @param func function for call before request real handler. If [func] returns true, then on next step will be
 * call original handler. If [func] returns false next call of original handler will not be executing
 * @return new router with preHandle
 */
fun Route.preHandle(func: suspend (Action) -> Boolean) = object : AbstractRoute() {
    override suspend fun execute(action: Action): Boolean {
        if (!func(action)) {
            return true
        }
        return super.execute(action)
    }
}

fun Route.postHandle(func: suspend (action: Action, result: Boolean) -> Unit) = object : AbstractRoute() {
    override suspend fun execute(action: Action): Boolean {
        val result = super.execute(action)
        func(action, result)
        return result
    }
}

/**
 * Wraping real call to original handler
 *
 * Example:
 * ```
 * router.wrap { action, handler->
 *      try {
 *          handler()
 *      } catch(e:Throwable){
 *          e.printStacktrace()
 *          action.resp.status = 500
 *          true
 *      }
 * }
 * ```
 *
 * @param func will be call instend original handler. As argument will be pass original [Action] and result of original handler
 */
fun Route.wrap(func: suspend (Action, suspend (Action) -> Boolean) -> Boolean) = object : AbstractRoute() {
    init {
        this@wrap.forward(this)
    }

    override suspend fun execute(action: Action): Boolean =
        func(action) { newAction ->
            super.execute(newAction)
        }
}