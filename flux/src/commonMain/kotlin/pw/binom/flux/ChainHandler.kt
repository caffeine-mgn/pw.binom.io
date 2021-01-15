package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class ChainHandler(
    private val nextChain: Handler,
    private val func: suspend (req: HttpRequest, resp: HttpResponse) -> Boolean
) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        if (func(req, resp)) {
            nextChain.request(req, resp)
        }
    }
}

fun Handler.chain(func: suspend (req: HttpRequest, resp: HttpResponse) -> Boolean) =
    ChainHandler(
        this,
        func
    )