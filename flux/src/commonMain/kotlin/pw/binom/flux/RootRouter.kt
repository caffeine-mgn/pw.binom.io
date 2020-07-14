package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class RootRouter : AbstractRoute(), Handler {

    private class ActionImpl(override val req: HttpRequest, override val resp: HttpResponse) : Action

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        resp.status = 404
        execute(ActionImpl(req, resp))
    }

}