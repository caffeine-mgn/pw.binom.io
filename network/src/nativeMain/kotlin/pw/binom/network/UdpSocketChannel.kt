package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

actual class UdpSocketChannel : Closeable {
    val native = NSocket.udp()
    var key: AbstractKey? = null
        set(value) {
            if (field != null) {
                field!!.removeSocket(native.raw)
            }
            field = value
            value?.addSocket(native.raw)
        }

    actual fun setBlocking(value: Boolean) {
        native.setBlocking(value)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        return native.send(data, address)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        return native.recv(data, address)
    }

    override fun close() {
        val c = key
        key = null
        c?.let {
            if (!it.closed) {
                it.close()
            }
        }
        native.close()
    }

    actual fun bind(address: NetworkAddress) {
//        check(native.port == null) { "Already bindded. port: $port" }
        native.bind(address)
    }

    actual val port: Int?
        get() = native.port
}
