package pw.binom.route

import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.Path
import pw.binom.url.URI

class MockHttpServerExchange(
  override val requestHeaders: Headers = emptyHeaders(),
  override val requestMethod: String,
  override val requestURI: URI,
  input: ByteArray = ByteArray(0),
) : HttpServerExchange {
  override val input: AsyncInput = ByteArrayInput(input).asyncInput()
  private val oo = ByteArrayOutput()
  val outputBytes
    get() = oo.toByteArray()
  override val output: AsyncOutput = oo.asyncOutput(callClose = false)
  private var internalResponseStarted = false
  override val responseStarted: Boolean
    get() = internalResponseStarted
  override val requestContext: Path = Path.EMPTY
  var statusCode = 0
    private set
  var responseHeaders: Headers = emptyHeaders()
    private set

  override suspend fun startResponse(statusCode: Int, headers: Headers) {
    check(!responseStarted)
    internalResponseStarted = true
    this.statusCode = statusCode
    this.responseHeaders = headers
  }
}
