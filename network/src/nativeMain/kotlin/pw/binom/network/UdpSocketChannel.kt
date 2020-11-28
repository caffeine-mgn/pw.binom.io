package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

actual class UdpSocketChannel : Closeable {
    val native = NSocket.udp()

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        native.send(data, address)

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int =
        native.recv(data, address)

    override fun close() {
        native.close()
    }

    actual fun bind(address: NetworkAddress) {
        native.bind(address)
    }
}