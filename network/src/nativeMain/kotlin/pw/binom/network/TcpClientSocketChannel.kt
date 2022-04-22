package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val connectable: Boolean) : Channel {
    var native: NSocket? = null

    //    var selector: AbstractSelector? = null
    var key: AbstractKey? = null
        set(value) {
            if (field != null && native != null) {
                field!!.removeSocket(native!!.raw)
            }
            field = value
            if (native != null) {
                value?.addSocket(native!!.raw)
            }
        }

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
        native = NSocket.connectTcp(address, blocking = false)
        native!!.setBlocking(blocking)
        key?.addSocket(native!!.raw)
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

        native?.also {
            val c = key
            key = null
            c?.let {
                if (!it.closed) {
                    it.close()
                }
            }
            it.close()
            native = null
        }
    }

    override fun write(data: ByteBuffer): Int =
        native!!.send(data)

    override fun flush() {
    }
}
