package pw.binom.io.socket

import pw.binom.io.BindException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class RawServerSocketChannel constructor(override val native:JServerSocketChannel) : ServerSocketChannel, NetworkChannel {

    actual constructor():this(JServerSocketChannel.open())

    override fun close() {
        native.close()
    }

    override fun bind(host: String, port: Int) {
        try {
            native.bind(InetSocketAddress(host, port))
        } catch (e: java.net.BindException) {
            throw BindException(e.message)
        }
    }

    override fun accept(): RawSocketChannel? {
        return RawSocketChannel(native.accept())
    }

    override var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
}