package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

actual class UdpSocketChannel : Closeable {
    val native = NSocket.udp()

    init {
        native.setBlocking(false)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        native.send(data, address)

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int =
        native.recv(data, address)

    override fun close() {
        native.close()
    }

    actual fun bind(address: NetworkAddress) {
//        check(native.port == null) { "Already bindded. port: $port" }
        native.bind(address)
    }

    actual val port: Int?
        get() = native.port
}