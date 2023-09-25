package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

interface ConnectionFactory {
    companion object {
        val DEFAULT: ConnectionFactory = object : ConnectionFactory {
            /*
            override suspend fun connect(
                networkManager: NetworkManager,
                schema: String,
                host: String,
                port: Int,
            ): AsyncChannel =
                try {
                    networkManager.tcpConnect(
                        NetworkAddress.create(
                            host = host,
                            port = port,
                        ),
                    )
                } catch (e: IOException) {
                    throw IOException("Can't connect to $host:$port", e)
                }
*/
            override suspend fun connect(channel: AsyncChannel, schema: String, host: String, port: Int): AsyncChannel =
                channel
        }
    }

    suspend fun connect(networkManager: NetworkManager, schema: String, host: String, port: Int): AsyncChannel {
        val channel = networkManager.tcpConnect(
            InetNetworkAddress.create(
                host = host,
                port = port,
            ),
        )

        return connect(
            channel = channel,
            schema = schema,
            host = host,
            port = port,
        )
    }

    suspend fun connect(channel: AsyncChannel, schema: String, host: String, port: Int): AsyncChannel = channel
}
