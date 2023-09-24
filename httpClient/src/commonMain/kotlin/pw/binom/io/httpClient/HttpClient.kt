package pw.binom.io.httpClient

import pw.binom.io.Closeable
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.Headers
import pw.binom.url.URL

interface HttpClient : Closeable {

  suspend fun startConnect(
    method: String,
    uri: URL,
    headers: Headers,
    requestLength: OutputLength = OutputLength.Chunked,
    keepAlive: Boolean? = true,
  ): HttpRequestBody

  suspend fun connect(method: String, uri: URL): HttpRequest
  suspend fun connect(method: HTTPMethod, uri: URL) =
    connect(method.code, uri)

  companion object {
    fun createDefault() = internalCreateHttpClient()
  }
}
