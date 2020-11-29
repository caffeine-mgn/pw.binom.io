package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel
import java.nio.channels.SocketChannel as JSocketChannel

actual class TcpClientSocketChannel : Channel {
    val native = JSocketChannel.open()

    init {
        native.configureBlocking(false)
    }

    actual constructor()

    actual fun connect(address: NetworkAddress) {
        val _native = address._native
        require(native != null)
        native.connect(_native)
//        native.finishConnect()
    }

    override fun read(dest: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }
}