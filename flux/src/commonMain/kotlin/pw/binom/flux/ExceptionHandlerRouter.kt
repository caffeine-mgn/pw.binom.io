package pw.binom.flux

import pw.binom.io.httpServer.Handler3Deprecated
import pw.binom.io.httpServer.HttpRequestDeprecated
import pw.binom.io.httpServer.HttpResponseDeprecated

class ExceptionHandlerRouter(
    private val defaultHandler: Handler3Deprecated,
    private val handler: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated, Throwable) -> Unit
) : Handler3Deprecated {

    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        try {
            defaultHandler.request(req, resp)
        } catch (e: Throwable) {
            handler(req, resp, e)
        }
    }
}

fun Handler3Deprecated.exceptionHandler(handler: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated, Throwable) -> Unit) =
    ExceptionHandlerRouter(this, handler)