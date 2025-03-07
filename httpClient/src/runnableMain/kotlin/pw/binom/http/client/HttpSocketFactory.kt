package pw.binom.http.client

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.url.URL

interface HttpSocketFactory {
    companion object {
        fun http(networkManager: NetworkManager): HttpSocketFactory =
            object : HttpSocketFactory {
                override suspend fun createSocketFactory(url: URL): AsyncChannel =
                    networkManager.tcpConnect(
                        DomainSocketAddress(
                            host = url.domain,
                            port = url.port ?: 80,
                        ).resolve()
                    )
            }
    }

    suspend fun createSocketFactory(url: URL): AsyncChannel
}
