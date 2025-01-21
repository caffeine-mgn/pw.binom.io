package pw.binom.io.httpClient

import pw.binom.io.http.Headers
import pw.binom.url.URL

interface HttpProxy {
  fun connect(url: URL)
  fun request(method: String, url: URL, headers: Headers)
}
