package pw.binom.io.socket

import pw.binom.io.BindException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class ServerSocketChannel actual constructor() : NetworkChannel {
    override fun close() {
        native.close()
    }

    internal val native = JServerSocketChannel.open()

    actual fun bind(host: String, port: Int) {
        try {
            native.bind(InetSocketAddress(host, port))
        } catch (e: java.net.BindException) {
            throw BindException(e.message)
        }
    }

    actual fun accept(): SocketChannel? {
        return SocketChannel(native.accept() ?: return null)
    }

    actual var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
}