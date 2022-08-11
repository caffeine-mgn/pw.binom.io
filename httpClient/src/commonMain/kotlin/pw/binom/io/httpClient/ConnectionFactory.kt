package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

fun interface ConnectionFactory {
    companion object {
        val DEFAULT: ConnectionFactory = ConnectionFactory { networkManager, schema, host, port ->
            networkManager.tcpConnect(
                NetworkAddress.Immutable(
                    host = host,
                    port = port
                )
            )
        }
    }

    suspend fun connect(networkManager: NetworkManager, schema: String, host: String, port: Int): AsyncChannel
}
