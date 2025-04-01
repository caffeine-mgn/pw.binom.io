package pw.binom.http.client.factory

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.SafeException
import pw.binom.io.AsyncChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.ssl.*

class SSLNetSocketFactory(
  val source: NetSocketFactory,
  val keyManager: KeyManager = EmptyKeyManager,
  val trustManager: TrustManager = TrustManager.TRUST_ALL,
  val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : NetSocketFactory {

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    }

    override suspend fun connect(host: String, port: Int): AsyncChannel =
        SafeException.async {
            val channel = source.connect(host = host, port = port).closeOnException()
            val sslSession = sslContext.clientSession(host = host, port = port).closeOnException()
            sslSession.asyncChannel(channel = channel, closeParent = true, bufferSize = sslBufferSize)
        }
}
