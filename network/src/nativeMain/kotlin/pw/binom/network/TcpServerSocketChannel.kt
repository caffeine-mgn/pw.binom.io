package pw.binom.network

import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable {
    var key: AbstractSelector.AbstractKey? = null
        set(value) {
            if (field != null && native != null) {
                field!!.removeSocket(native!!.raw)
            }
            field = value
            if (native != null) {
                value?.addSocket(native!!.raw)
            }
        }

    var native: NSocket? = null

    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? {
        val socket = native!!.accept(address) ?: return null
        return TcpClientSocketChannel(socket)
    }

    actual fun bind(address: NetworkAddress) {
        if (native != null) {
            throw IllegalStateException()
        }
        native = NSocket.serverTcp(address)
        native!!.setBlocking(false)
        key?.addSocket(native!!.raw)
    }

    override fun close() {
        native?.also {
            key?.removeSocket(it.raw)
            it.close()
        }
    }

    actual val port: Int?
        get() = native!!.port
}
