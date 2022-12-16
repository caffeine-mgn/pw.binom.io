package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val connectable: Boolean) : Channel, NetworkChannel {
    private var internalNative: NSocket? = null
    override val native: RawSocket?
        get() = internalNative?.raw
    override val nNative: NSocket?
        get() = internalNative

    private var currentKey: AbstractNativeKey? = null

    override fun setKey(key: AbstractNativeKey) {
        if (this.currentKey === key) {
            native?.let { key.setRaw(it) }
            return
        }
        this.currentKey?.close()
        this.currentKey = key
        native?.let { key.setRaw(it) }
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

    actual constructor() : this(true)
    constructor(socket: NSocket) : this(false) {
        this.internalNative = socket
    }

    private var blocking = true
    actual fun setBlocking(value: Boolean) {
        internalNative?.setBlocking(value)
        blocking = value
    }

    private fun checkConnectable() {
        check(connectable) { "Socket is not connectable" }
    }

    actual fun connect(address: NetworkAddressOld) {
        checkConnectable()
        internalNative = NSocket.connectTcp(address, blocking = blocking)
//        native!!.setBlocking(blocking)
        currentKey?.setRaw(native!!)
//        println("TcpClientSocketChannel:: connection to $address. currentKey=$currentKey")
    }

    override fun read(dest: ByteBuffer): Int {
        val read = internalNative!!.recv(dest)
        if (read == -1) {
            close()
            return -1
        }
        return read
    }

    override fun close() {
        currentKey?.close()
        internalNative?.close()
    }

    override fun write(data: ByteBuffer): Int =
        internalNative!!.send(data)

    override fun flush() {
        // Do nothing
    }

    actual fun connect(fileName: String) {
        checkConnectable()
        internalNative = NSocket.connectTcpUnixSocket(fileName = fileName, blocking = false)
        internalNative!!.setBlocking(blocking)
        currentKey?.setRaw(native!!)
    }

    internal fun connected() {
//        println("Tcp Connection connected!!! currentKey=$currentKey")
        currentKey?.setRaw(native!!)
    }
}
