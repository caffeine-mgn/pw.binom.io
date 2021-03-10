package pw.binom.flux

import pw.binom.io.httpServer.HttpRequest2
import pw.binom.io.httpServer.HttpRequestDeprecated
import pw.binom.io.httpServer.HttpResponseDeprecated

interface Action {
    val req: HttpRequest2
}