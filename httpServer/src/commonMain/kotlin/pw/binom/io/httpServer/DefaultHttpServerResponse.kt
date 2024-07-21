package pw.binom.io.httpServer

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.HttpAsyncOutput
import pw.binom.io.http.HttpInput
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

  override suspend fun writeBinary(): HttpAsyncOutput {
    val out = startOutput()
    return object : HttpAsyncOutput {
      override suspend fun getInput(): HttpInput? = null

      override suspend fun write(data: ByteBuffer) = out.write(data)

      override suspend fun asyncClose() {
        out.asyncClose()
      }

      override suspend fun flush() {
        out.flush()
      }
    }
  }
}
