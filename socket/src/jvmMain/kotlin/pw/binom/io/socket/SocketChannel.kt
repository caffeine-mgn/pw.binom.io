package pw.binom.io.socket

import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel as JSocketChannel

actual class SocketChannel constructor(internal val native: JSocketChannel) : Channel, OutputStream, InputStream {
    override fun flush() {
        //NOP
    }

    override fun close() {
        native.close()
    }

    actual constructor() : this(JSocketChannel.open())

    actual fun connect(host: String, port: Int) {
        native.connect(InetSocketAddress(host, port))
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val buffer = ByteBuffer.wrap(data, offset, length)
        val r = native.read(buffer)
        if (r == -1)
            throw SocketClosedException()
        return r
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (data.isEmpty())
            return 0
        if (offset + length > data.size)
            throw ArrayIndexOutOfBoundsException()
        try {
            val buffer: ByteBuffer = ByteBuffer.wrap(data, offset, length)
            return native.write(buffer)
        } catch (e: IOException) {
            throw pw.binom.io.IOException()
        }
    }

    actual var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
}