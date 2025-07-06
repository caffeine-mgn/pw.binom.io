package pw.binom.http.client

import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.IOException
import pw.binom.io.bufferedReader
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.url.URL

class SseRequestBuilder(
  val client: HttpClientCommon,
  val headers: MutableHeaders,
  var method: String,
  var bufferSize: Int,
  var url: URL,
) {
  suspend fun connect(): SseConnection {
    val exchange = client.connect(
      method = method,
      url = url,
      headers = headers,
    ) as Http11ClientExchange
    return exchange.toSSE(bufferSize=bufferSize)
//    val responseCode = exchange.getResponseCode()
//    val responseHeaders = exchange.getResponseHeaders()
//    val (input, output) = exchange.toRaw()
//    val charset = responseHeaders.getSingleOrNull(Headers.CHARSET)?.let { Charsets.get(it) } ?: Charsets.UTF8
//    val reader = input.bufferedReader(charset = charset, bufferSize = bufferSize)
//    return SseConnectionImpl(
//      responseCode = responseCode,
//      responseHeaders = responseHeaders,
//      input = reader,
//      output = output,
//    )
  }
}
