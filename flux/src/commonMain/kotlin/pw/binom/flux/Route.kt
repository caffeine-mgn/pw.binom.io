package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.Handler3Deprecated

interface Route {
    fun route(path: String, route: Route)
    fun route(path: String, func: (Route.() -> Unit)? = null): Route
    fun detach(path: String, route: Route)
    fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean): Closeable
    fun endpoint(method: HTTPMethod, path: String, func: suspend (Action) -> Boolean): Closeable =
        endpoint(
            method = method.code,
            path = path,
            func = func
        )

    fun forward(handler: Handler?)
    suspend fun execute(action: Action): Boolean
}

inline fun Route.get(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.GET, path, func)

inline fun Route.head(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.HEAD, path, func)

inline fun Route.patch(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.PATCH, path, func)

inline fun Route.trace(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.TRACE, path, func)

inline fun Route.options(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.OPTIONS, path, func)

inline fun Route.connect(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.CONNECT, path, func)

inline fun Route.post(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.POST, path, func)

inline fun Route.put(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.PUT, path, func)

inline fun Route.delete(path: String, noinline func: suspend (Action) -> Boolean) =
    endpoint(HTTPMethod.DELETE, path, func)

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

abstract class RouteWrapper(val route: Route) : AbstractRoute() {
    init {
        route.forward(this)
    }


    protected suspend fun invokeSuper(action: Action) =
        super.execute(action)


    override suspend fun execute(action: Action): Boolean = wraping(action)

    abstract suspend fun wraping(action: Action): Boolean
}