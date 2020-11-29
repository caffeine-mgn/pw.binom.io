package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

actual class UdpSocketChannel : Closeable {
    val native = DatagramChannel.open()
    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        val _native = address._native
        require(_native != null)
        return native.send(data.native, _native)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        val before = data.capacity
        if (before == 0) {
            return 0
        }
        val vv = native.receive(data.native)
        address?._native = vv as InetSocketAddress
        return before - data.capacity
    }

    actual fun bind(address: NetworkAddress) {
        val _native = address._native
        require(_native != null)
        native.bind(_native)
    }

    override fun close() {
        native.close()
    }
}