package pw.binom.flux

import pw.binom.io.Closeable
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

interface Route {
    val serialization: Serialization
    fun route(path: String, route: Route)
    fun route(path: String, func: (Route.() -> Unit)? = null): Route
    fun detach(path: String, route: Route)
    fun endpoint(method: String, path: String, func: suspend (FluxHttpRequest) -> Unit): Closeable
    fun endpoint(method: HTTPMethod, path: String, func: suspend (FluxHttpRequest) -> Unit): Closeable =
        endpoint(
            method = method.code,
            path = path,
            func = func
        )

    fun forward(handler: Handler?)
    suspend fun execute(action: HttpRequest)
}

fun Route.get(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.GET, path, func)

fun Route.head(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.HEAD, path, func)

fun Route.patch(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.PATCH, path, func)

fun Route.trace(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.TRACE, path, func)

fun Route.options(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.OPTIONS, path, func)

fun Route.connect(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.CONNECT, path, func)

fun Route.post(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.POST, path, func)

fun Route.put(path: String, func: suspend (FluxHttpRequest) -> Unit) =
    endpoint(HTTPMethod.PUT, path, func)

fun Route.delete(path: String, func: suspend (FluxHttpRequest) -> Unit) =
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
fun Route.preHandle(func: suspend (HttpRequest) -> Boolean) = object : AbstractRoute() {
    override val serialization: Serialization
        get() = this@preHandle.serialization

    override suspend fun execute(action: HttpRequest) {
        func(action)
        if (action.response == null) {
            super.execute(action)
        }
    }
}

fun Route.postHandle(func: suspend (action: HttpRequest) -> Unit) = object : AbstractRoute() {
    override val serialization: Serialization
        get() = this@postHandle.serialization
    override suspend fun execute(action: HttpRequest) {
        super.execute(action)
        func(action)
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
fun Route.wrap(func: suspend (HttpRequest, suspend (HttpRequest) -> Unit) -> Unit) = object : AbstractRoute() {

    init {
        this@wrap.forward(this)
    }

    override val serialization: Serialization
        get() = this@wrap.serialization

    override suspend fun execute(action: HttpRequest) =
        func(action) { newAction ->
            super.execute(newAction)
        }
}

abstract class RouteWrapper(val route: Route) : AbstractRoute() {
    init {
        route.forward(this)
    }


    protected suspend fun invokeSuper(action: HttpRequest) =
        super.execute(action)


    override suspend fun execute(action: HttpRequest) = wraping(action)

    abstract suspend fun wraping(action: HttpRequest)
}