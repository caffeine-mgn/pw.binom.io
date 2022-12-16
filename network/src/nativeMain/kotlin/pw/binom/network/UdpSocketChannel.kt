package pw.binom.network

import pw.binom.io.ByteBuffer

actual class UdpSocketChannel : NetworkChannel {
    private val internlNative = NSocket.udp()
    override val native: RawSocket
        get() = internlNative.raw
    override val nNative: NSocket
        get() = internlNative

    private var currentKey: AbstractNativeKey? = null

    override fun setKey(key: AbstractNativeKey) {
        if (this.currentKey === key) {
            return
        }
        this.currentKey?.close()
        this.currentKey = key
        key.setRaw(native)
    }

    override fun keyClosed() {
        currentKey = null
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

    actual fun send(data: ByteBuffer, address: NetworkAddressOld): Int {
        return internlNative.send(data, address)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddressOld.Mutable?): Int {
        return internlNative.recv(data, address)
    }

    override fun close() {
        currentKey?.close()
        internlNative.close()
    }

    actual fun bind(address: NetworkAddressOld) {
//        check(native.port == null) { "Already bindded. port: $port" }
        internlNative.bind(address)
    }

    actual val port: Int?
        get() = internlNative.port
}
