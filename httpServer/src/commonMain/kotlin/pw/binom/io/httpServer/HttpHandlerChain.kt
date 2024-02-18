package pw.binom.io.httpServer

fun interface HttpHandlerChain {
  @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
  suspend fun handle(
    exchange: HttpServerExchange,
    next: HttpHandler,
  )
}
