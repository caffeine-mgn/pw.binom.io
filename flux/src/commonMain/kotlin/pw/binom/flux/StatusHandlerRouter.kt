package pw.binom.flux

import pw.binom.io.httpServer.Handler3Deprecated
import pw.binom.io.httpServer.HttpRequestDeprecated
import pw.binom.io.httpServer.HttpResponseDeprecated

class StatusHandlerRouter(
    private val defaultHandler: Handler3Deprecated,
    private val status: Int,
    private val handler: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated) -> Unit
) : Handler3Deprecated {

    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        defaultHandler.request(req, resp)
        if (resp.status == status) {
            handler
        }
    }
}

fun Handler3Deprecated.statusHandler(status: Int, handler: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated) -> Unit) =
    StatusHandlerRouter(this, status, handler)