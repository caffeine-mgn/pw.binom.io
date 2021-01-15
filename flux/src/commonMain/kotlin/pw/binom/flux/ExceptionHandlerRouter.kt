package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class ExceptionHandlerRouter(
    private val defaultHandler: Handler,
    private val handler: suspend (req: HttpRequest, resp: HttpResponse, Throwable) -> Unit
) : Handler {

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        try {
            defaultHandler.request(req, resp)
        } catch (e: Throwable) {
            handler(req, resp, e)
        }
    }
}

fun Handler.exceptionHandler(handler: suspend (req: HttpRequest, resp: HttpResponse, Throwable) -> Unit) =
    ExceptionHandlerRouter(this, handler)