package pw.binom.io.http

import pw.binom.io.httpClient.HttpRequest
import pw.binom.url.URL

interface ClientRouteConfig : RouteConfig {
  fun createUrl(suffix: String): URL
  fun connect(method: String, url: URL): HttpRequest
}
