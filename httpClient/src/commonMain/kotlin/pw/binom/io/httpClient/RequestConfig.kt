package pw.binom.io.httpClient

import pw.binom.io.http.BasicAuth
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.useBasicAuth
import pw.binom.url.URL

interface RequestConfig {
  val headers: MutableHeaders
  val url: URL
  val method: String
}

fun <T : RequestConfig> T.setHeader(key: String, value: String): T {
  headers[key] = value
  return this
}

fun <T : RequestConfig> T.addHeader(key: String, value: String): T {
  headers.add(key, value)
  return this
}

fun <T : RequestConfig> T.addCookie(key: String, value: String): T {
  headers.add(Headers.COOKIE, "$key=$value")
  return this
}

fun <T : RequestConfig> T.use(basicAuth: BasicAuth): T {
  headers.useBasicAuth(basicAuth)
  return this
}
