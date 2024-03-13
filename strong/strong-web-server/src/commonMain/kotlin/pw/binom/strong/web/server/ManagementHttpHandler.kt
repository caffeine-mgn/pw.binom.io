package pw.binom.strong.web.server

import pw.binom.io.httpServer.HttpServerExchange

fun interface ManagementHttpHandler {
  @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
  suspend fun handle(exchange: HttpServerExchange)
}
