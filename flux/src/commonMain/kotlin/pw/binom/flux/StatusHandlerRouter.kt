package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class StatusHandlerRouter(
    private val defaultHandler: Handler,
    private val status: Int,
    private val handler: suspend (req: HttpRequest, resp: HttpResponse) -> Unit
) : Handler {

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        defaultHandler.request(req, resp)
        if (resp.status == status) {
            handler
        }
    }
}

fun Handler.statusHandler(status: Int, handler: suspend (req: HttpRequest, resp: HttpResponse) -> Unit) =
    StatusHandlerRouter(this, status, handler)