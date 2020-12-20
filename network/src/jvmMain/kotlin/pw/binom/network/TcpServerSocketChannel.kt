package pw.binom.network

import pw.binom.io.Closeable
import java.net.BindException as JBindException
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class TcpServerSocketChannel : Closeable {
    val native = JServerSocketChannel.open()

    init {
        native.configureBlocking(false)
    }

    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? =
        native.accept()?.let { TcpClientSocketChannel(it) }

    actual fun bind(address: NetworkAddress) {
        try {
            val _native = address._native
            require(_native != null)
            native.bind(_native)
        } catch (e: JBindException) {
            throw BindException("Address already in use: ${address.host}:${address.port}")
        }
    }

    override fun close() {
        native.close()
    }
}