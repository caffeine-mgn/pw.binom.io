package pw.binom.io.socket

import pw.binom.io.BindException
import pw.binom.io.Closeable
import pw.binom.io.SocketException
import java.net.InetSocketAddress
import java.net.ServerSocket as JServerSocket

actual class SocketServer : Closeable {
    override fun close() {
        native.close()
    }

    internal val native = JServerSocket()
    actual fun bind(port: Int) {
        try {
            native.bind(InetSocketAddress(port))
        } catch (e: java.net.BindException) {
            throw BindException()
        } catch (e: java.net.SocketException) {
            throw SocketException(e.message)
        }
    }

    actual fun accept(): Socket? = Socket(native.accept())
}