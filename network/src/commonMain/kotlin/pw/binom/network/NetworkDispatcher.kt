package pw.binom.network

import pw.binom.io.Closeable
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.JvmName

class NetworkDispatcher : Closeable {
    val selector = Selector.open()

    @JvmName("jvmWait")
    fun wait(timeout: Long = -1L) {
        selector.select(timeout) { key, mode ->
            println("!event!  ${mode.toString(2)}")
            val connection = key.attachment as AbstractConnection
            if (mode and Selector.EVENT_CONNECTED != 0) {
                connection.connected()
            }
            if (mode and Selector.EVENT_ERROR != 0) {
                connection.error()
            }
            if (mode and Selector.EVENT_EPOLLIN != 0) {
                connection.readyForRead()
            }
            if (mode and Selector.EVENT_EPOLLOUT != 0) {
                connection.readyForWrite()
            }
        }
    }

    suspend fun tcpConnect(address: NetworkAddress) {
        val channel = TcpClientSocketChannel()
        channel.connect(address)
        val connection = attach(channel)
        suspendCoroutine<Unit> {
            connection.connect = it
        }
    }

    fun attach(channel: TcpClientSocketChannel): TcpConnection {
        val con = TcpConnection(channel)
        selector.attach(channel, 0, con)
        return con
    }

    override fun close() {
        selector.close()
    }
}