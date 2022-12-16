package pw.binom.network

import pw.binom.io.Closeable

actual class TcpServerSocketChannel : Closeable, NetworkChannel {

    private var currentKey: AbstractNativeKey? = null

    override fun setKey(key: AbstractNativeKey) {
        if (this.currentKey === key) {
            return
        }
        this.currentKey?.close()
        this.currentKey = key
        native?.let { nt -> key.setRaw(nt) }
    }

    override fun keyClosed() {
        currentKey = null
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

    actual fun accept(address: NetworkAddressOld.Mutable?): TcpClientSocketChannel? {
        val socket = internalNative!!.accept(address) ?: return null
        return TcpClientSocketChannel(socket)
    }

    actual fun bind(address: NetworkAddressOld) {
        if (native != null) {
            throw IllegalStateException()
        }
        internalNative = NSocket.serverTcp(address)
        internalNative!!.setBlocking(blocking)
        currentKey?.setRaw(native!!)
    }

    override fun close() {
        currentKey?.close()
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
        currentKey?.setRaw(native!!)
    }
}
