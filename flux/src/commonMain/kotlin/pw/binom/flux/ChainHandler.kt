package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

class ChainHandler(
    private val nextChain: Handler,
    private val func: suspend (req: HttpRequest) -> Boolean
) : Handler {
    override suspend fun request(req: HttpRequest) {
        if (func(req)) {
            nextChain.request(req)
        }
    }
}

fun Handler.chain(func: suspend (req: HttpRequest) -> Boolean) =
    ChainHandler(
        this,
        func
    )
