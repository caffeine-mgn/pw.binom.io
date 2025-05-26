package pw.binom.io.httpServer

import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.bufferedWriter
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders

val HttpServerExchange.isAcceptSSE: Boolean
  get() {
    val accept = this.requestHeaders[Headers.ACCEPT] ?: return false
    return "text/event-stream" in accept
  }

suspend fun HttpServerExchange.sse(charset: Charset = Charsets.UTF8, headers: Headers = emptyHeaders()): SseConnection {
  val responseHeader = HashHeaders2()
  responseHeader[Headers.CONTENT_TYPE] = "text/event-stream"
  responseHeader[Headers.CONNECTION] = Headers.CLOSE
  responseHeader[Headers.CHARSET] = charset.name
  responseHeader.addAll(headers)
  startResponse(statusCode = 101, headers = responseHeader)
  return SseConnectionImpl(
    mainChannel = mainChannel,
    output = output.bufferedWriter(charset = charset),
  )
}
