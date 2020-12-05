package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val native: NSocket, val connectable: Boolean) : Channel {
    actual constructor() : this(NSocket.tcp(), true)

    init {
        native.setBlocking(false)
    }

    actual fun connect(address: NetworkAddress) {
        native.connect(address)
    }

    override fun read(dest: ByteBuffer): Int{
        val read = native.recv(dest)
        return read
    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
        native.send(data)

    override fun flush() {

    }
}