package pw.binom.http.client

import pw.binom.url.URL

interface HttpConnectionFactory {
    companion object {
        val NOT_SUPPORTED = object : HttpConnectionFactory {
            override suspend fun connect(
              url: URL,
              source: NetSocketFactory,
              pushBack: suspend (HttpConnection) -> Unit,
            ): HttpConnection {
                throw IllegalStateException("Can't connect to $url: not supported")
            }
        }
    }

    suspend fun connect(
      url: URL,
      source: NetSocketFactory,
      pushBack: suspend (HttpConnection) -> Unit,
    ): HttpConnection
}
