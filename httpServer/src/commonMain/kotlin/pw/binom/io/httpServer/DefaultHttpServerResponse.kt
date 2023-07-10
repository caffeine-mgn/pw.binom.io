package pw.binom.io.httpServer

import pw.binom.io.AsyncOutput
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.MutableHeaders

class DefaultHttpServerResponse(val exchange: HttpServerExchange) : HttpServerResponse {
  override val headers: MutableHeaders = HashHeaders2()

  override var status: Int = 404
  override val responseStarted: Boolean
    get() = exchange.responseStarted

  override suspend fun startOutput(): AsyncOutput {
    exchange.startResponse(status, headers)
    return exchange.output
  }
}
