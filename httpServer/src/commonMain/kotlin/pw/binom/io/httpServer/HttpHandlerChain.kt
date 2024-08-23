package pw.binom.io.httpServer

fun interface HttpHandlerChain {
  suspend fun handle(
    exchange: HttpServerExchange,
    next: HttpHandler,
  )
}
