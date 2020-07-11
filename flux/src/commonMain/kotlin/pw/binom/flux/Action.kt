package pw.binom.flux

import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse

interface Action {
    val req: HttpRequest
    val resp: HttpResponse
}