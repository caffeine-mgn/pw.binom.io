package pw.binom.http.rest

import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.Path
import pw.binom.url.URI

class StubHttpServerExchange(
  override val requestURI: URI,
  override val requestMethod: String,
  override val requestContext: Path = Path.EMPTY,
  input: ByteArray = byteArrayOf(),
): HttpServerExchange {
  override val input: AsyncInput = ByteArrayInput(input).asyncInput(callClose = false)
  override var responseStarted: Boolean = false
    private set

  fun header(name: String, value: String): StubHttpServerExchange {
    requestHeaders.add(key = name, value = value)
    return this
  }

  override val requestHeaders = HashHeaders2()
  fun getResponseData() = outputStream.toByteArray()
  private val outputStream = ByteArrayOutput()
  override val output: AsyncOutput = outputStream.asyncOutput(callClose = false)

  override suspend fun startResponse(statusCode: Int, headers: Headers) {
    check(!responseStarted)
    responseStarted = true
  }
}
