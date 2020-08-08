package pw.binom.flux

import pw.binom.io.httpServer.Handler

interface Route {
    fun route(path: String, func: (Route.() -> Unit)? = null): Route
    fun endpoint(method: String, path: String, func: suspend (Action) -> Boolean)
    fun forward(handler: Handler)
}

inline fun Route.get(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("GET", path, func)
inline fun Route.post(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("POST", path, func)
inline fun Route.put(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("PUT", path, func)
inline fun Route.delete(path: String, noinline func: suspend (Action) -> Boolean) = endpoint("DELETE", path, func)