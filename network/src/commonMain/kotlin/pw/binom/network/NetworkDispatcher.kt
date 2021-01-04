package pw.binom.network

import pw.binom.io.Closeable
import kotlin.coroutines.suspendCoroutine

class NetworkDispatcher : Closeable {
    val selector = Selector.open()

    fun select(timeout: Long = -1L) =
        selector.select(timeout) { key, mode ->
            val connection = key.attachment as AbstractConnection
            if (mode and Selector.EVENT_CONNECTED != 0) {
                connection.connected()
            }
            if (mode and Selector.EVENT_ERROR != 0) {
                connection.error()
            }
            if (mode and Selector.INPUT_READY != 0) {
                connection.readyForRead()
            }
            if (mode and Selector.OUTPUT_READY != 0) {
                connection.readyForWrite()
            }
            println("update! ${mode.toString(2)}, write: [${mode and Selector.OUTPUT_READY != 0}], read: [${mode and Selector.INPUT_READY != 0}], connected: [${mode and Selector.EVENT_CONNECTED != 0}]")
        }

    suspend fun tcpConnect(address: NetworkAddress): TcpConnection {
        val channel = TcpClientSocketChannel()
        channel.connect(address)
        val connection = attach(channel)
        suspendCoroutine<Unit> {
            connection.connect = it
        }
        return connection
    }

    fun bindTcp(address: NetworkAddress): TcpServerConnection {
        val channel = TcpServerSocketChannel()
        channel.bind(address)
        return attach(channel)
    }

    fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        val con = TcpServerConnection(this, channel)
        con.key = selector.attach(channel, 0, con)
        return con
    }

    fun bindUDP(address: NetworkAddress): UdpConnection {
        val channel = UdpSocketChannel()
        channel.bind(address)
        return attach(channel)
    }

    fun openUdp(): UdpConnection {
        val channel = UdpSocketChannel()
        return attach(channel)
    }

    fun attach(channel: UdpSocketChannel): UdpConnection {
        val con = UdpConnection(channel)
        con.holder = CrossThreadKeyHolder(selector.attach(channel, 0, con))
        return con
    }

    fun attach(channel: TcpClientSocketChannel): TcpConnection {
        val con = TcpConnection(channel)
        con.holder = CrossThreadKeyHolder(selector.attach(channel, 0, con))
        return con
    }

    override fun close() {
        selector.close()
    }
}