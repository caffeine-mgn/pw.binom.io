package pw.binom.network

import pw.binom.io.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

actual class UdpSocketChannel : NetworkChannel {
    val native = DatagramChannel.open()

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        val _native = address._native
        require(_native != null)
        return native.send(data.native, _native)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        val before = data.position
        if (before == data.remaining) {
            return 0
        }
        val vv = native.receive(data.native)
        if (vv != null) {
            address?._native = vv as InetSocketAddress
        }
        return data.position - before
    }

    private var bindPort: Int? = null

    actual fun bind(address: NetworkAddress) {
        check(bindPort == null) { "Already bindded" }
        val _native = address._native
        require(_native != null)
//        bindPort = native.bind(_native).socket().port
        bindPort = native.bind(_native).socket().localPort
    }

    override fun close() {
        native.close()
    }

    actual val port: Int?
        get() = bindPort

    actual fun setBlocking(value: Boolean) {
        native.configureBlocking(value)
    }
}
