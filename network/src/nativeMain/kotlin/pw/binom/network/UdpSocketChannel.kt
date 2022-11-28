package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.ByteBuffer

actual class UdpSocketChannel : NetworkChannel {
    private val internlNative = NSocket.udp()
    override val native: RawSocket
        get() = internlNative.raw
    override val nNative: NSocket
        get() = internlNative
    private var keys = defaultMutableSet<AbstractKey>()
    override fun addKey(key: AbstractKey) {
        if (keys.add(key)) {
            key.addSocket(native)
        }
    }

    override fun removeKey(key: AbstractKey) {
        if (keys.remove(key)) {
            key.removeSocket(native)
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
        internlNative.setBlocking(value)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        return internlNative.send(data, address)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        return internlNative.recv(data, address)
    }

    override fun close() {
        keys.forEach {
            it.internalCleanSocket()
            it.close()
        }
        keys.clear()
        internlNative.close()
    }

    actual fun bind(address: NetworkAddress) {
//        check(native.port == null) { "Already bindded. port: $port" }
        internlNative.bind(address)
    }

    actual val port: Int?
        get() = internlNative.port
}
