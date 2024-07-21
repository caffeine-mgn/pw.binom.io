package pw.binom.io.httpClient

import pw.binom.io.http.Headers
import pw.binom.url.URL

class JsBaseHttpClient : HttpClient {
  override suspend fun startConnect(
    method: String,
    uri: URL,
    headers: Headers,
    requestLength: OutputLength,
    keepAlive: Boolean?,
  ) = JsHttpRequestBody(method = method, url = uri, headers = headers)

  override suspend fun connect(method: String, uri: URL) = JsHttpRequest(
    client = this,
    method = method,
    url = uri
  )

  override fun close() {

  }
}
