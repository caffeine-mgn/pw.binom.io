package pw.binom.io.httpClient

import pw.binom.io.*
import pw.binom.io.http.*

interface HttpRequest : AsyncCloseable, RequestConfig, HttpOutput {
  var request: String

  /**
   * Starts write binary request
   * Closes this [HttpRequest] and delegate control to returned [AsyncHttpRequestOutput].
   */
  override suspend fun writeBinary(): AsyncHttpRequestOutput

  suspend fun writeBinaryAndGetResponse(func: suspend (AsyncHttpRequestOutput) -> Unit): HttpResponse {
    val resp = writeBinary()
    func(resp)
    return resp.getResponse()
  }

  /**
   * Starts write text request
   * Closes this [HttpRequest] and delegate control to returned [AsyncHttpRequestWriter].
   */
  override suspend fun writeText(): AsyncHttpRequestWriter

  suspend fun writeTextAndGetResponse(func: suspend (AsyncHttpRequestWriter) -> Unit): HttpResponse {
    val e = writeText()
    func(e)
    return e.getResponse()
  }

  /**
   * Starts to get HTTP response
   * Closes this [DefaultHttpRequest] and delegate control to returned [HttpResponse].
   */
  suspend fun getResponse(): HttpResponse

  suspend fun <T> useResponse(func: suspend (HttpResponse) -> T): T = getResponse().useAsync { func(it) }
}

internal fun generateWebSocketHeaders(self: HttpRequest): Headers {
  val host = self.url.host + (self.url.port?.let { ":$it" } ?: "")
  return headersOf(
    Headers.ORIGIN to host,
    Headers.HOST to host,
  )
}

interface AsyncHttpRequestOutput : HttpAsyncOutput {
  override suspend fun getInput(): HttpResponse

  suspend fun getResponse(): HttpResponse = getInput()
}

interface AsyncHttpRequestWriter : HttpAsyncWriter {
  override suspend fun getInput(): HttpResponse

  suspend fun getResponse() = getInput()
}
