package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Channel
import java.io.IOException
import java.nio.channels.SocketChannel as JSocketChannel

actual class TcpClientSocketChannel(val native: JSocketChannel) : Channel {

    init {
        native.configureBlocking(false)
    }

    actual constructor() : this(JSocketChannel.open())

    actual fun connect(address: NetworkAddress) {
        val _native = address._native
        require(_native != null)
        native.connect(_native)
    }

    override fun read(dest: ByteBuffer): Int {
        val count = try {
            native.read(dest.native)
        } catch (e: Throwable) {
            throw SocketClosedException(e)
        }
        if (count < 0) {
            throw SocketClosedException()
        }
        return count
    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
        try {
            val ret = native.write(data.native)
            if (ret < 0) {
                throw SocketClosedException()
            }
            ret
        } catch (e: IOException) {
            throw SocketClosedException()
        }

    override fun flush() {
    }
}