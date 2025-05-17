package pw.binom.http.client.factory

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.SafeException
import pw.binom.http.client.Http11ConnectionImpl
import pw.binom.http.client.HttpConnection
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.ssl.*
import pw.binom.url.URL
import pw.binom.url.UrlHelper

class Https11ConnectionFactory(
  val keyManager: KeyManager = EmptyKeyManager,
  val trustManager: TrustManager = TrustManager.TRUST_ALL,
  val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
  val autoFlushSize: Int = DEFAULT_BUFFER_SIZE,
  val fallback: HttpConnectionFactory = Http11ConnectionFactory(),
) : HttpConnectionFactory {

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    }

    override suspend fun connect(url: URL, source: NetSocketFactory, pushBack: suspend (HttpConnection) -> Unit): HttpConnection {

      val schema: String
      val port: Int
      val host:String
      UrlHelper.parseUrl(
        url.fullPath,
        schema = { schema = it },
        port = { port = it?:443 },
        domain = {host=it}
      )

//        val schema = url.schema
        if (schema != "https" && schema != "wss") {
            return fallback.connect(url = url, source = source, pushBack = pushBack)
        }
//        val host = url.domain
//        val port = url.port ?: 443
        val stream = SafeException.async {
            val channel = source.connect(host = host, port = port).closeOnException()
            val sslSession = sslContext.clientSession(host = host, port = port).closeOnException()
            sslSession.asyncChannel(channel = channel, closeParent = true, bufferSize = sslBufferSize)
        }
        return Http11ConnectionImpl(
            channel = AsyncAsciiChannel(
                channel = stream,
                bufferSize = autoFlushSize,
            ),
            autoFlushSize = autoFlushSize,
            pushBack = pushBack,
        )
    }
}

