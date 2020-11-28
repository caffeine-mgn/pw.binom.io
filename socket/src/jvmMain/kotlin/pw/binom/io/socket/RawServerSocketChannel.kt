package pw.binom.io.socket

import pw.binom.io.BindException
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class RawServerSocketChannel constructor(override val native: JServerSocketChannel) : ServerSocketChannel,
    NetworkChannel {

    actual constructor() : this(JServerSocketChannel.open())

    override val selectableChannel: SelectableChannel
        get() = native

    override val accepteble: Boolean
        get() = true

    override fun close() {
        native.close()
    }

    override fun bind(address: NetworkAddress) {
        try {
            native.bind(InetSocketAddress(address.host, address.port))
        } catch (e: java.net.BindException) {
            throw BindException("${e.message}, host:[${address.host}], port: [${address.port}]")
        }
    }

    override fun accept(): RawSocketChannel? {
        return native.accept()?.let { RawSocketChannel(it) }
    }

    override var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
}