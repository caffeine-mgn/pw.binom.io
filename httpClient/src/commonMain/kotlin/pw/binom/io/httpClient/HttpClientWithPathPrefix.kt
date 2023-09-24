package pw.binom.io.httpClient

import pw.binom.io.http.Headers
import pw.binom.url.Path
import pw.binom.url.URL
import pw.binom.url.toPath
import kotlin.jvm.JvmName

class HttpClientWithPathPrefix(val original: HttpClient, val prefix: Path) : HttpClient {

  private fun makeUrl(uri: URL) = uri.copy(path = prefix.append(uri.path))

  override suspend fun startConnect(
    method: String,
    uri: URL,
    headers: Headers,
    requestLength: OutputLength,
    keepAlive: Boolean?,
  ) = original.startConnect(
    method = method,
    uri = makeUrl(uri),
    headers = headers,
    requestLength = requestLength,
    keepAlive = keepAlive,
  )

  override suspend fun connect(method: String, uri: URL) =
    original.connect(
      method = method,
      uri = makeUrl(uri)
    )

  override fun close() {
    original.close()
  }
}

@JvmName("withPrefixPath")
fun HttpClient.withPrefix(prefix: Path) = HttpClientWithPathPrefix(
  original = this,
  prefix = prefix,
)

fun HttpClient.withPrefix(prefix: String) = withPrefix(
  prefix = prefix.toPath,
)
