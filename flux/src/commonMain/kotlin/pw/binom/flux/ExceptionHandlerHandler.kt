package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

class ExceptionHandlerHandler(
    private val defaultHandler: Handler,
    private val handler: suspend (req: HttpRequest, Throwable) -> Unit
) : Handler {

    override suspend fun request(req: HttpRequest) {
        try {
            defaultHandler.request(req)
        } catch (e: Throwable) {
            handler(req, e)
        }
    }
}

fun Handler.exceptionHandler(handler: suspend (req: HttpRequest, Throwable) -> Unit) =
    ExceptionHandlerHandler(this, handler)
