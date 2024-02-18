package pw.binom.io.httpServer

private class ChainHttpHandlerImpl(val chain: HttpHandlerChain, val next: HttpHandler) : HttpHandler {
  override suspend fun handle(exchange: HttpServerExchange) {
    chain.handle(exchange, next)
  }
}

fun interface HttpHandler {
  companion object {
    fun chain(
      chains: List<HttpHandlerChain>,
      next: HttpHandler,
    ) = chains.foldRight(next) { element, acc ->
      ChainHttpHandlerImpl(chain = element, acc)
    }
  }

  @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
  suspend fun handle(exchange: HttpServerExchange)
}
