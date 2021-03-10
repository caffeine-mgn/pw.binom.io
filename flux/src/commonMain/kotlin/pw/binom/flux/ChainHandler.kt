package pw.binom.flux

import pw.binom.io.httpServer.Handler3Deprecated
import pw.binom.io.httpServer.HttpRequestDeprecated
import pw.binom.io.httpServer.HttpResponseDeprecated

class ChainHandler(
    private val nextChain: Handler3Deprecated,
    private val func: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated) -> Boolean
) : Handler3Deprecated {
    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
        if (func(req, resp)) {
            nextChain.request(req, resp)
        }
    }
}

fun Handler3Deprecated.chain(func: suspend (req: HttpRequestDeprecated, resp: HttpResponseDeprecated) -> Boolean) =
    ChainHandler(
        this,
        func
    )