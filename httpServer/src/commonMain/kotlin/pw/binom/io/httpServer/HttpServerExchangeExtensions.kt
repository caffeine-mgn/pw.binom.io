package pw.binom.io.httpServer

inline fun <T> HttpServerExchange.response(func: HttpServerResponse.() -> T) =
  response().let(func)
