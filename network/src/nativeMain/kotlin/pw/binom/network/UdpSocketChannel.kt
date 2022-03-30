package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

actual class UdpSocketChannel : Closeable {
    val native = NSocket.udp()
    var key: AbstractSelector.AbstractKey? = null
        set(value) {
            if (field != null) {
                field!!.removeSocket(native.raw)
            }
            field = value
            println("udp: add socket to key")
            value?.addSocket(native.raw)
        }

    init {
        native.setBlocking(false)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        return native.send(data, address)
    }

    actual fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        return native.recv(data, address)
    }

    override fun close() {
        key?.removeSocket(native.raw)
        native.close()
    }

    actual fun bind(address: NetworkAddress) {
//        check(native.port == null) { "Already bindded. port: $port" }
        println("Bind native to $address")
        native.bind(address)
        println("port after bind: ${native.port}")
    }

    actual val port: Int?
        get() = native.port
}
