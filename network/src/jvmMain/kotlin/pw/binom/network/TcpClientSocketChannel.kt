package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import java.io.IOException
import java.nio.channels.NotYetConnectedException
import java.nio.channels.SocketChannel as JSocketChannel

actual class TcpClientSocketChannel(val native: JSocketChannel) : Channel {

    actual constructor() : this(JSocketChannel.open())

    actual fun setBlocking(value: Boolean) {
        native.configureBlocking(value)
    }

    actual fun connect(address: NetworkAddress) {
        val _native = address._native
        require(_native != null)
        native.connect(_native)
    }

    private var disconneced = false
    override fun read(dest: ByteBuffer): Int {
        if (disconneced) {
            return -1
        }
        val count = try {
            native.read(dest.native)
        } catch (e: IOException) {
            runCatching { native.close() }
            disconneced = true
            return -1
        } catch (e: NotYetConnectedException) {
            return 0
        }
        if (count < 0 || count == 0 && !native.isConnected) {
            runCatching { native.close() }
            return -1
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
