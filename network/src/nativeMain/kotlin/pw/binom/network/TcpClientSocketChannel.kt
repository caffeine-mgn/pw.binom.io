package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val connectable: Boolean) : Channel {
    var native: NSocket? = null

    //    var selector: AbstractSelector? = null
    var key: AbstractSelector.AbstractKey? = null
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
        socket.setBlocking(false)
    }

    actual fun connect(address: NetworkAddress) {
        if (!connectable) {
            throw IllegalStateException()
        }
        native = NSocket.connectTcp(address, blocking = false)
        key?.addSocket(native!!.raw)
    }

    override fun read(dest: ByteBuffer): Int {
        val read = native!!.recv(dest)
        return read
    }

    override fun close() {
        native?.also {
            key?.removeSocket(it.raw)
            it.close()
        }
    }

    override fun write(data: ByteBuffer): Int =
        native!!.send(data)

    override fun flush() {
    }
}
