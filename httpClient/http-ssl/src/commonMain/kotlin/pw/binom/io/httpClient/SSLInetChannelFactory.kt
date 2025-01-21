package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.SafeException
import pw.binom.io.AsyncChannel
import pw.binom.io.httpClient.channel.InetChannelFactory
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.ssl.*

class SSLInetChannelFactory<T : AsyncChannel>(
  private val keyManager: KeyManager = EmptyKeyManager,
  private val trustManager: TrustManager = TrustManager.TRUST_ALL,
  private val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
  private val factory: InetChannelFactory<T>,
) : InetChannelFactory<AsyncSSLChannel> {

  private val sslContext: SSLContext by lazy {
    SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
  }

  override suspend fun free(channel: AsyncSSLChannel) {
    channel.asyncClose()
  }

  override suspend fun connect(address: DomainSocketAddress): AsyncSSLChannel =
    SafeException.async {
      val channel = factory.connect(address).closeOnException()
      val sslSession = sslContext.clientSession(
        host = address.host,
        port = address.port,
      )
      val sslChannel = sslSession.asyncChannel(
        channel = channel,
        closeParent = true,
        bufferSize = sslBufferSize,
      )
      sslChannel
    }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
