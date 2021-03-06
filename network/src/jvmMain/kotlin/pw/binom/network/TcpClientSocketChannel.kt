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
        try {
            val count = native.read(dest.native)
            if (count >= 0) {
                return count
            }
            throw SocketClosedException()
        } catch (e:Throwable){
            println("-----------")
            e.printStackTrace()
            println("-----------")
            throw SocketClosedException()
        }
    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
        try {
            var ret = native.write(data.native)
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