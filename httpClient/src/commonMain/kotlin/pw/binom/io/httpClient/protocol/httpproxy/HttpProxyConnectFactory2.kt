package pw.binom.io.httpClient.protocol.httpproxy

import pw.binom.io.AsyncChannel
import pw.binom.io.http.HttpAuth
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkManager

class HttpProxyConnectFactory2(
    val proxyUrl: NetworkAddress,
    val networkManager: NetworkManager,
    val protocolSelector: ProtocolSelector,
    val auth: HttpAuth?,
) : ConnectFactory2 {
    override fun createConnect(): HttpConnect =
        HttpProxyConnect(
            proxyUrl = proxyUrl,
            networkManager = networkManager,
            tcp = null,
            protocolSelector = protocolSelector,
            auth = auth,
        )

    override fun createConnect(channel: AsyncChannel): HttpConnect =
        HttpProxyConnect(
            proxyUrl = proxyUrl,
            networkManager = networkManager,
            tcp = channel,
            protocolSelector = protocolSelector,
            auth = auth,
        )

    override val supportOverConnection: Boolean
        get() = true
}
