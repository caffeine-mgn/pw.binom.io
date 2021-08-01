package pw.binom.network

import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable {
    val native = NSocket.tcp()

    init {
        native.setBlocking(false)
    }

    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? {
        val socket = native.accept(address) ?: return null
        return TcpClientSocketChannel(socket, false)
    }

    actual fun bind(address: NetworkAddress) {
        native.bind(address)
    }

    override fun close() {
        native.close()
    }

    actual val port: Int
        get() = native.port ?: -1
}