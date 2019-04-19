package pw.binom.io.socket

import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class ServerSocketChannel actual constructor() : Channel {
    override fun close() {
        native.close()
    }

    internal val native = JServerSocketChannel.open()

    actual fun bind(port: Int) {
        native.bind(InetSocketAddress(port))
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