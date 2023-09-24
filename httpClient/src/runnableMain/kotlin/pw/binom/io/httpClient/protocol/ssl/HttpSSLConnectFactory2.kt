package pw.binom.io.httpClient.protocol.ssl

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.network.NetworkManager
import pw.binom.ssl.*

class HttpSSLConnectFactory2(
    val keyManager: KeyManager = EmptyKeyManager,
    val trustManager: TrustManager = TrustManager.TRUST_ALL,
    val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val protocolSelector: ProtocolSelector,
    val networkManager: NetworkManager,
) : ConnectFactory2 {

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    }

    override fun createConnect(): HttpConnect =
        HttpSSLConnect(
            channel = null,
            sslBufferSize = sslBufferSize,
            protocolSelector = protocolSelector,
            networkManager = networkManager,
            sslContext = sslContext,
        )

    override fun createConnect(channel: AsyncChannel): HttpConnect =
        HttpSSLConnect(
            channel = channel,
            sslBufferSize = sslBufferSize,
            protocolSelector = protocolSelector,
            networkManager = networkManager,
            sslContext = sslContext,
        )

    override val supportOverConnection: Boolean
        get() = true
}
