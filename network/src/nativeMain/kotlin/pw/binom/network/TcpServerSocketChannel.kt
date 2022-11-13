package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable, NetworkChannel {

    private var keys = defaultMutableSet<AbstractKey>()

    override fun addKey(key: AbstractKey) {
        val native = native
        val added = keys.add(key)
        if (added && native != null) {
            key.addSocket(native!!.raw)
        }
    }

    override fun removeKey(key: AbstractKey) {
        val native = native
        val removed = keys.remove(key)
        if (removed && native != null) {
            key.removeSocket(native.raw)
        }
    }

//    var key: AbstractKey? = null
//        set(value) {
//            if (field != null && native != null) {
//                field!!.removeSocket(native!!.raw)
//            }
//            field = value
//            if (native != null) {
//                value?.addSocket(native!!.raw)
//            }
//        }

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
        native!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!.raw)
        }
    }

    override fun close() {
        keys.forEach {
            it.internalCleanSocket()
            it.close()
        }
        keys.clear()
        native?.close()
    }

    actual val port: Int?
        get() = native!!.port

    private var blocking = true

    actual fun setBlocking(value: Boolean) {
        native?.setBlocking(value)
        blocking = value
    }

    actual fun bind(fileName: String) {
        if (native != null) {
            throw IllegalStateException()
        }
        native = NSocket.serverTcpUnixSocket(fileName)
        native!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!.raw)
        }
    }
}
