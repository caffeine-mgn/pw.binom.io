package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.ByteBuffer

actual class UdpSocketChannel : NetworkChannel {
    val native = NSocket.udp()
    private var keys = defaultMutableSet<AbstractKey>()
    override fun addKey(key: AbstractKey) {
        if (keys.add(key)) {
            key.addSocket(native.raw)
        }
    }

    override fun removeKey(key: AbstractKey) {
        if (keys.remove(key)) {
            key.removeSocket(native.raw)
        }
    }
//    var key: AbstractKey? = null
//        set(value) {
//            if (field != null) {
//                field!!.removeSocket(native.raw)
//            }
//            field = value
//            value?.addSocket(native.raw)
//        }

    actual fun setBlocking(value: Boolean) {
        native.setBlocking(value)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        return native.send(data, address)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        return native.recv(data, address)
    }

    override fun close() {
        keys.forEach {
            it.internalCleanSocket()
            it.close()
        }
        keys.clear()
        native.close()
    }

    actual fun bind(address: NetworkAddress) {
//        check(native.port == null) { "Already bindded. port: $port" }
        native.bind(address)
    }

    actual val port: Int?
        get() = native.port
}
