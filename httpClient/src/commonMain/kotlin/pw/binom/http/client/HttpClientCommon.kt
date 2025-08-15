package pw.binom.http.client

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.HashHeaders
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.url.URL

interface HttpClientCommon : AsyncCloseable {
  suspend fun connect(method: String, url: URL, headers: Headers = emptyHeaders()): HttpClientCommonExchange
  fun request(method: String, url: URL, headers: Headers = emptyHeaders()): HttpRequestBuilder =
    HttpRequestBuilder(
      url = url,
      method = method,
      client = this,
      headers = HashHeaders().addAll(headers)
    )
}
