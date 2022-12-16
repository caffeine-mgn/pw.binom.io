package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import pw.binom.io.socket.Selector
import pw.binom.io.socket.TcpClientSocket
import pw.binom.io.socket.TcpNetServerSocket
import pw.binom.io.socket.UdpNetSocket

abstract class AbstractNetworkManager : CoroutineDispatcher(), NetworkManager {
    protected abstract val selector: Selector
    protected abstract fun ensureOpen()

    override fun attach(channel: TcpClientSocket, mode: Int): TcpConnection {
        ensureOpen()
        channel.blocking = false
        val key = selector.attach(socket = channel)
        val con = TcpConnection(channel = channel, currentKey = key)
        key.listenFlags = mode
        key.attachment = con
        if (mode != 0) {
            selector.wakeup()
        }
        return con
    }

    override fun attach(channel: TcpNetServerSocket): TcpServerConnection {
        ensureOpen()
        channel.blocking = false
        val key = selector.attach(socket = channel)
        val con = TcpServerConnection(channel = channel, dispatcher = this, currentKey = key)
        key.listenFlags = 0
        key.attachment = con
        return con
    }

    override fun attach(channel: UdpNetSocket): UdpConnection {
        ensureOpen()
        val con = UdpConnection(channel)
        channel.blocking = false
        val key = selector.attach(socket = channel)
        key.listenFlags = 0
        key.attachment = con
        con.keys.addKey(key)
        return con
    }

    override fun wakeup() {
        ensureOpen()
        selector.wakeup()
    }
}
