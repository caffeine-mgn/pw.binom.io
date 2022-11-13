package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val connectable: Boolean) : Channel, NetworkChannel {
    var native: NSocket? = null

    private var keys = defaultMutableSet<AbstractKey>()

    override fun addKey(key: AbstractKey) {
        if (keys.add(key) && native != null) {
            key.addSocket(native!!.raw)
        }
    }

    override fun removeKey(key: AbstractKey) {
        if (keys.remove(key) && native != null) {
            key.removeSocket(native!!.raw)
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
        this.native = socket
    }

    private var blocking = true
    actual fun setBlocking(value: Boolean) {
        native?.setBlocking(value)
        blocking = value
    }

    actual fun connect(address: NetworkAddress) {
        if (!connectable) {
            throw IllegalStateException()
        }
        native = NSocket.connectTcp(address, blocking = blocking)
//        native!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!.raw)
        }
    }

    override fun read(dest: ByteBuffer): Int {
        val read = native!!.recv(dest)
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
        native?.close()
    }

    override fun write(data: ByteBuffer): Int =
        native!!.send(data)

    override fun flush() {
    }

    actual fun connect(fileName: String) {
        if (!connectable) {
            throw IllegalStateException()
        }
        native = NSocket.connectTcpUnixSocket(fileName = fileName, blocking = false)
        native!!.setBlocking(blocking)
        keys.forEach {
            it.addSocket(native!!.raw)
        }
    }

    internal fun connected() {
        keys.forEach {
            it.addSocket(native!!.raw)
        }
    }
}
