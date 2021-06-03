package pw.binom.flux

import pw.binom.io.httpServer.*

class StatusHandlerRouter(
    private val defaultHandler: Handler,
    private val status: Int,
    private val handler: suspend (req: HttpRequest) -> Unit
) : Handler {

    override suspend fun request(req: HttpRequest) {
        defaultHandler.request(req)

        if ((req.response == null && status == 404) || req.response?.status == 404) {
            handler(req)
        }
    }
}

fun Handler.statusHandler(
    status: Int,
    handler: suspend (req: HttpRequest) -> Unit
) =
    StatusHandlerRouter(this, status, handler)