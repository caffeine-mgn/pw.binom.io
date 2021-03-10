package pw.binom.flux

import pw.binom.io.httpServer.*

class ExceptionHandlerRouter(
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
    ExceptionHandlerRouter(this, handler)