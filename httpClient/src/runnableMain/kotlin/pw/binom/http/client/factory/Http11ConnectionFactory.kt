package pw.binom.http.client.factory

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.http.client.Http11ConnectionImpl
import pw.binom.http.client.HttpConnection
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.url.URL

class Http11ConnectionFactory(
  val autoFlushSize: Int = DEFAULT_BUFFER_SIZE,
  val fallback: HttpConnectionFactory = HttpConnectionFactory.NOT_SUPPORTED,
) : HttpConnectionFactory {
    override suspend fun connect(
      url: URL,
      source: NetSocketFactory,
      pushBack: suspend (HttpConnection) -> Unit
    ): HttpConnection {
        val schema = url.schema
        if (schema != "http" && schema != "ws") {
            return fallback.connect(url = url, source = source, pushBack = pushBack)
        }
        return Http11ConnectionImpl(
            channel = AsyncAsciiChannel(
                source.connect(
                    host = url.domain,
                    port = url.port ?: 80
                ),
                bufferSize = autoFlushSize,
            ),
            autoFlushSize = autoFlushSize,
            pushBack = pushBack,
        )
    }
}
