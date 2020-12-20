package pw.binom.io.socket

import pw.binom.io.BindException
import pw.binom.io.SocketException
import java.net.InetSocketAddress
import java.net.ServerSocket as JServerSocket

actual class RawSocketServer(internal val native:JServerSocket): SocketServer {

    override fun close() {
        native.close()
    }

    actual constructor():this(JServerSocket())

    override fun bind(host: String, port: Int) {
        try {
            native.bind(InetSocketAddress(host, port))
        } catch (e: java.net.BindException) {
            throw BindException()
        } catch (e: java.net.SocketException) {
            throw SocketException(e.message)
        }
    }

    override fun accept(): RawSocket? = RawSocket(native.accept())
}