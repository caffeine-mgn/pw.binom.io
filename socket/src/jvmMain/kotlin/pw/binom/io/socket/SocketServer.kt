package pw.binom.io.socket

import pw.binom.io.Closeable
import java.net.InetSocketAddress
import java.net.ServerSocket as JServerSocket

actual class SocketServer : Closeable {
    override fun close() {
        native.close()
    }

    internal val native = JServerSocket()
    actual fun bind(port: Int) {
        native.bind(InetSocketAddress(port))
    }

    actual fun accept(): Socket? = Socket(native.accept())
}