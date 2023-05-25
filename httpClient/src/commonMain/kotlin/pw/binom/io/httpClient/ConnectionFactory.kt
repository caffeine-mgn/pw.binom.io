package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

fun interface ConnectionFactory {
    companion object {
        val DEFAULT: ConnectionFactory = ConnectionFactory { networkManager, schema, host, port ->
            networkManager.tcpConnect(
                NetworkAddress.create(
                    host = host,
                    port = port,
                ),
            )
        }
    }

    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun connect(networkManager: NetworkManager, schema: String, host: String, port: Int): AsyncChannel
}
