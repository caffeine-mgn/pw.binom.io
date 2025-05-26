package pw.binom.http.client

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.url.URL

fun HttpClientCommon.sseRequest(
  url: URL,
  headers: Headers = emptyHeaders(),
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
): SseRequestBuilder {
  val newHeaders = HashHeaders2()
  newHeaders[Headers.CONNECTION] = Headers.CLOSE
  newHeaders[Headers.HOST] = url.domain
  newHeaders[Headers.ACCEPT] = "text/event-stream"
  newHeaders.addAll(headers)
  return SseRequestBuilder(
    client = this,
    headers = newHeaders,
    method = "GET",
    url = url,
    bufferSize = bufferSize,
  )
}
