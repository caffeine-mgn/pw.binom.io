package pw.binom.io.socket

import pw.binom.io.*
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel as JSocketChannel

actual class SocketChannel constructor(internal val native: JSocketChannel) : NetworkChannel, OutputStream, InputStream {
    override fun flush() {
        //NOP
    }

    override fun close() {
        native.close()
    }

    actual constructor() : this(JSocketChannel.open())

    actual fun connect(host: String, port: Int) {
        try {
            native.connect(InetSocketAddress(host, port))
            native.finishConnect()
        } catch (e: java.net.UnknownHostException) {
            throw UnknownHostException(e.message!!)
        } catch (e: java.net.ConnectException) {
            throw ConnectException(host, port)
        }
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val buffer = ByteBuffer.wrap(data, offset, length)
        try {
            val r = native.read(buffer)
            if (r == -1)
                throw SocketClosedException()
            return r
        } catch (e: java.io.IOException) {
            throw IOException(e.message)
        }
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (data.isEmpty())
            return 0
        if (offset + length > data.size)
            throw ArrayIndexOutOfBoundsException()
        try {
            val buffer: ByteBuffer = ByteBuffer.wrap(data, offset, length)
            return native.write(buffer)
        } catch (e: java.io.IOException) {
            throw IOException(e.message)
        }
    }

    actual var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
    actual val isConnected: Boolean
        get() = native.isConnected
}