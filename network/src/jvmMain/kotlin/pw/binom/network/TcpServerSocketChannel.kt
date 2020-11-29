package pw.binom.network

import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable {
    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? = null
    actual fun bind(address: NetworkAddress) {

    }

    override fun close() {
        TODO("Not yet implemented")
    }
}