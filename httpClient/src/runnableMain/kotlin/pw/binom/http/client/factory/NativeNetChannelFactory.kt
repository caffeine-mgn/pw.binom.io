package pw.binom.http.client.factory

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

class NativeNetChannelFactory(val manager: NetworkManager) : NetSocketFactory {
    override suspend fun connect(host: String, port: Int): AsyncChannel =
        manager.tcpConnect(
            DomainSocketAddress(
                host = host,
                port = port,
            ).resolve()
        )
}
