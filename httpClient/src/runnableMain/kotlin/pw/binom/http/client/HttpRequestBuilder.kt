package pw.binom.http.client

import pw.binom.io.http.MutableHeaders
import pw.binom.url.URL

class HttpRequestBuilder(
  var url: URL,
  var method: String,
  private val client: HttpClientRunnable,
  val headers: MutableHeaders,
) {
    suspend fun connect() = client.connect(
        method = method,
        url = url,
        headers = headers,
    )
}
