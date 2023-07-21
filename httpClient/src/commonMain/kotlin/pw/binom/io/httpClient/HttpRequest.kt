package pw.binom.io.httpClient

import pw.binom.io.*
import pw.binom.io.http.*

interface HttpRequest : AsyncCloseable, RequestConfig {
  var request: String

  /**
   * Starts write binary request
   * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestOutput].
   */
  suspend fun writeData(): AsyncHttpRequestOutput
  suspend fun writeData(data: ByteBuffer) = writeData {
    it.writeFully(data)
  }

  suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): HttpResponse {
    val resp = writeData()
    func(resp)
    return resp.getResponse()
  }

  /**
   * Starts write text request
   * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestWriter].
   */
  suspend fun writeText(): AsyncHttpRequestWriter
  suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): HttpResponse {
    val e = writeText()
    func(e)
    return e.getResponse()
  }

  /**
   * Starts to get HTTP response
   * Closes this [DefaultHttpRequest] and delegate control to returned [HttpResponse].
   */
  suspend fun getResponse(): HttpResponse
  suspend fun <T> useResponse(func: suspend (HttpResponse) -> T): T =
    getResponse().use { func(it) }
}

internal fun generateWebSocketHeaders(self: HttpRequest): Headers {
  val host = self.url.host + (self.url.port?.let { ":$it" } ?: "")
  return headersOf(
    Headers.ORIGIN to host,
    Headers.HOST to host,
  )
}

interface AsyncHttpRequestOutput : AsyncOutput {
  suspend fun getResponse(): HttpResponse
}

interface AsyncHttpRequestWriter : AsyncWriter {
  suspend fun getResponse(): HttpResponse
}
