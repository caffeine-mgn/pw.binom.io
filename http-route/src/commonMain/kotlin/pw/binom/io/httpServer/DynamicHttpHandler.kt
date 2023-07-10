package pw.binom.io.httpServer

@Suppress("UNCHECKED_CAST")
abstract class DynamicHttpHandler : HttpHandler {
  override suspend fun handle(exchange: HttpServerExchange) {
    throw RuntimeException("Are you sure you applied plugin")
  }

  protected open fun getDefaultHttpSerializer(): HttpSerializer {
    throw IllegalStateException("Default HttpSerializer not defined")
  }
}
