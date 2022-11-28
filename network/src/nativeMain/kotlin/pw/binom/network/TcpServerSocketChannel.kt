package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable, NetworkChannel {

    private var keys = defaultMutableSet<AbstractKey>()

    override fun addKey(key: AbstractKey) {
        val native = native
        val added = keys.add(key)
        if (added && native != null) {
            key.addSocket(native)
        }
    }

    override fun removeKey(key: AbstractKey) {
        val native = native
        val removed = keys.remove(key)
        if (removed && native != null) {
            key.removeSocket(native)
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

    var internalNative: NSocket? = null
    override val native: RawSocket?
        get() = internalNative?.raw
    override val nNative: NSocket?
        get() = internalNative

    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? {
        val socket = internalNative!!.accept(address) ?: return null
        return TcpClientSocketChannel(socket)
    }

    actual fun bind(address: NetworkAddress) {
        if (native != null) {
            throw IllegalStateException()
        }
        internalNative = NSocket.serverTcp(address)
        internalNative!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!)
        }
    }

    override fun close() {
        keys.forEach {
            it.internalCleanSocket()
            it.close()
        }
        keys.clear()
        internalNative?.close()
    }

    actual val port: Int?
        get() = internalNative!!.port

    private var blocking = true

    actual fun setBlocking(value: Boolean) {
        internalNative?.setBlocking(value)
        blocking = value
    }

    actual fun bind(fileName: String) {
        if (native != null) {
            throw IllegalStateException()
        }
        internalNative = NSocket.serverTcpUnixSocket(fileName)
        internalNative!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!)
        }
    }
}
