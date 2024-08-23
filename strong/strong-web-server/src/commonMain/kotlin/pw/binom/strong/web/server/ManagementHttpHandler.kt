package pw.binom.strong.web.server

import pw.binom.io.httpServer.HttpServerExchange

fun interface ManagementHttpHandler {
  suspend fun handle(exchange: HttpServerExchange)
}
