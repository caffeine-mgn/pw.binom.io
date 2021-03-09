package pw.binom.flux

import pw.binom.io.httpServer.Handler3
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class StatusHandlerRouter(
    private val defaultHandler: Handler3,
    private val status: Int,
    private val handler: suspend (req: HttpRequest, resp: HttpResponse) -> Unit
) : Handler3 {

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        defaultHandler.request(req, resp)
        if (resp.status == status) {
            handler
        }
    }
}

fun Handler3.statusHandler(status: Int, handler: suspend (req: HttpRequest, resp: HttpResponse) -> Unit) =
    StatusHandlerRouter(this, status, handler)