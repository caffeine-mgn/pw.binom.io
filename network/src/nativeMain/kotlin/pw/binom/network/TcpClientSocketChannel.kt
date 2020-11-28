package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel

actual class TcpClientSocketChannel(val native: NSocket) : Channel {
    actual constructor() : this(NSocket.tcp())

    actual fun connect(address: NetworkAddress) {
        native.connect(address)
    }

    override fun read(dest: ByteBuffer): Int =
        native.recv(dest)

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
        native.send(data)

    override fun flush() {

    }
}