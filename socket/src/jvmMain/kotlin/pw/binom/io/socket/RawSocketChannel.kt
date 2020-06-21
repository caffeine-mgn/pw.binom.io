package pw.binom.io.socket

import pw.binom.ByteDataBuffer
import pw.binom.io.ConnectException
import pw.binom.io.IOException
import pw.binom.io.UnknownHostException
import pw.binom.update
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel as JSocketChannel

actual class RawSocketChannel constructor(override val native: JSocketChannel) : SocketChannel, NetworkChannel {

    actual constructor() : this(JSocketChannel.open())

    override fun flush() {
        //NOP
    }

    override fun skip(length: Long): Long {
        TODO("Not yet implemented")
    }

    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        return data.update(offset, length) { b ->
            try {
                val r = native.read(b)
                if (r == -1)
                    throw SocketClosedException()
                 r
            } catch (e: java.io.IOException) {
                throw IOException(e.message)
            }
        }
    }

    override fun close() {
        native.close()
    }

    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        try {
            return data.update(offset, length) { data ->
                native.write(data)
            }
        } catch (e: java.io.IOException) {
            throw SocketClosedException()
        }
    }

    override fun connect(host: String, port: Int) {
        try {
            native.connect(InetSocketAddress(host, port))
            native.finishConnect()
        } catch (e: java.net.UnknownHostException) {
            throw UnknownHostException(e.message!!)
        } catch (e: java.net.ConnectException) {
            throw ConnectException(host, port)
        }
    }

    /*
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
    */
    override var blocking: Boolean
        get() = native.isBlocking
        set(value) {
            native.configureBlocking(value)
        }
    override val isConnected: Boolean
        get() = native.isConnected
}