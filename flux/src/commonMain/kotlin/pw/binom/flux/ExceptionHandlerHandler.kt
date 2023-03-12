package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

@Deprecated(message = "Use HttpRouting")
class ExceptionHandlerHandler(
    private val defaultHandler: Handler,
    private val handler: suspend (req: HttpRequest, Throwable) -> Unit,
) : Handler {

    override suspend fun request(req: HttpRequest) {
        try {
            defaultHandler.request(req)
        } catch (e: Throwable) {
            handler(req, e)
        }
    }
}

@Deprecated(message = "Use HttpRouting")
fun Handler.exceptionHandler(handler: suspend (req: HttpRequest, Throwable) -> Unit) =
    ExceptionHandlerHandler(this, handler)
