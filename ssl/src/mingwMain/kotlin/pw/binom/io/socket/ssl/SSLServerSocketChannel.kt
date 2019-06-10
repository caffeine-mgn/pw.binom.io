package pw.binom.io.socket.ssl

import pw.binom.io.socket.NetworkChannel
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketChannel

actual class SSLServerSocketChannel(val raw: SSLSocketServer) : ServerSocketChannel, NetworkChannel {
    override val nsocket: RawSocket
        get() = raw.raw.socket

    override fun bind(host: String, port: Int) {
        raw.bind(host, port)
    }

    override fun accept(): SocketChannel? {
        return SSLSocketChannel(raw.accept()?:return null)
    }

    override var blocking: Boolean
        get() = raw.raw.blocking
        set(value) {
            raw.raw.blocking = value
        }

    override fun close() {
        raw.close()
    }

}