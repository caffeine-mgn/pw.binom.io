package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val connectable: Boolean) : Channel, NetworkChannel {
    private var internalNative: NSocket? = null
    override val native: RawSocket?
        get() = internalNative?.raw
    override val nNative: NSocket?
        get() = internalNative

    private var keys = defaultMutableSet<AbstractKey>()

    override fun addKey(key: AbstractKey) {
        val raw = native
        if (keys.add(key) && raw != null) {
            key.addSocket(raw)
        }
    }

    override fun removeKey(key: AbstractKey) {
        val raw = native
        if (keys.remove(key) && raw != null) {
            key.removeSocket(raw)
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

    actual constructor() : this(true)
    constructor(socket: NSocket) : this(false) {
        this.internalNative = socket
    }

    private var blocking = true
    actual fun setBlocking(value: Boolean) {
        internalNative?.setBlocking(value)
        blocking = value
    }

    actual fun connect(address: NetworkAddress) {
        if (!connectable) {
            throw IllegalStateException()
        }
        internalNative = NSocket.connectTcp(address, blocking = blocking)
//        native!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!)
        }
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
        keys.forEach {
            it.internalCleanSocket()
            it.close()
        }
        keys.clear()
        internalNative?.close()
    }

    override fun write(data: ByteBuffer): Int =
        internalNative!!.send(data)

    override fun flush() {
    }

    actual fun connect(fileName: String) {
        if (!connectable) {
            throw IllegalStateException()
        }
        internalNative = NSocket.connectTcpUnixSocket(fileName = fileName, blocking = false)
        internalNative!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!)
        }
    }

    internal fun connected() {
        keys.forEach {
            it.addSocket(native!!)
        }
    }
}
