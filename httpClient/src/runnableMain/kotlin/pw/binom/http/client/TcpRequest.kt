package pw.binom.http.client

import pw.binom.io.AsyncChannel
import pw.binom.io.IOException
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.httpClient.HttpMetrics
import pw.binom.url.URL

class TcpRequest2(val url: URL, val method: String, val client: HttpClientRunnable) {
  val headers = HashHeaders2()
  private var started = false
  suspend fun connect(): AsyncChannel {
    check(!started) { "Connection already started" }
    check(headers.keepAlive == null || headers.keepAlive == false) { "keepAlive should be false or null" }
    started = true
    val request = client.connect(method = method, url = url, headers = headers)
    if (request.getResponseCode() != 101) {
      throw IOException("Invalid Response code: ${request.getResponseCode()}")
    }
    HttpMetrics.defaultHttpRequestCountMetric.dec()
    return AsyncChannel.create(
      input = request.getInput(),
      output = request.getOutput(),
    )
  }
}

fun HttpClientRunnable.tcpRequest(
  url: URL,
  headers: Headers = emptyHeaders(),
  method: String = "GET",
): TcpRequest2 {
  val req = TcpRequest2(
    url = url,
    method = method,
    client = this,
  )
  req.headers[Headers.CONNECTION] = Headers.UPGRADE
  req.headers[Headers.UPGRADE] = Headers.TCP
  req.headers[Headers.HOST] = url.host.toString()
  req.headers.keepAlive = false
  req.headers.addAll(headers)
  return req
}
