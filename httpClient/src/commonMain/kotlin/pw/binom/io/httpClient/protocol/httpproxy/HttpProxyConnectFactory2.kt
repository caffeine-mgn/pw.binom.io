package pw.binom.io.httpClient.protocol.httpproxy

import pw.binom.io.AsyncChannel
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.network.NetworkManager
import pw.binom.url.URL

class HttpProxyConnectFactory2(
    val proxyUrl: URL,
    val networkManager: NetworkManager,
    val protocolSelector: ProtocolSelector,
) : ConnectFactory2 {
    override fun createConnect(): HttpConnect =
        HttpProxyConnect(
            proxyUrl = proxyUrl,
            networkManager = networkManager,
            tcp = null,
            protocolSelector = protocolSelector,
        )

    override fun createConnect(channel: AsyncChannel): HttpConnect =
        HttpProxyConnect(
            proxyUrl = proxyUrl,
            networkManager = networkManager,
            tcp = channel,
            protocolSelector = protocolSelector,

        )

    override val supportOverConnection: Boolean
        get() = true
}
