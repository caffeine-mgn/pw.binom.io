package pw.binom.flux

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

class RootRouter : AbstractRoute() {
    interface ExceptionHandler {
        fun exception(exception: Throwable)
    }
}