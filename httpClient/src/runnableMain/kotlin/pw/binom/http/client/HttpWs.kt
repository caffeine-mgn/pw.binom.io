package pw.binom.http.client

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.url.URL

fun HttpClientCommon.wsRequest(
  url: URL,
  headers: Headers = emptyHeaders(),
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  masking: Boolean = true,
): WsRequestBuilder {
  val newHeaders = HashHeaders2()
  newHeaders[Headers.CONNECTION] = Headers.CLOSE
  newHeaders[Headers.HOST] = url.domain
  newHeaders.addAll(headers)
  return WsRequestBuilder(
    client = this,
    headers = newHeaders,
    method = "GET",
    url = url,
    bufferSize = bufferSize,
    masking = masking,
  )
}
