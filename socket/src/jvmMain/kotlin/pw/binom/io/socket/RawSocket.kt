package pw.binom.io.socket

import pw.binom.io.*
import java.net.InetSocketAddress
import java.net.Socket as JSocket

actual class RawSocket constructor(val native: JSocket) : Socket {

    private inner class SInputStream : InputStream {
        override fun read(data: ByteArray, offset: Int, length: Int): Int =
                try {
                    val r = native.getInputStream().read(data, offset, length)
                    if (r == -1) {
                        this@RawSocket.close()
                        throw SocketClosedException()
                    }
                    r
                } catch (e: java.io.IOException) {
                    throw IOException(e.message)
                }

        override fun close() {
        }
    }

    private inner class SOutputStream : OutputStream {
        override fun write(data: ByteArray, offset: Int, length: Int): Int {
            if (closed)
                throw SocketClosedException()
            if (!connected)
                throw IOException("Socket is not connected")
            if (length == 0)
                return 0
            if (offset + length > data.size)
                throw ArrayIndexOutOfBoundsException()
            native.getOutputStream().write(data, offset, length)
            return length
        }

        override fun flush() {
            native.getOutputStream().flush()
        }

        override fun close() {
        }

    }

    override val input: InputStream = SInputStream()
    override val output: OutputStream = SOutputStream()

    actual constructor() : this(JSocket())

    override fun close() {
        native.close()
    }

    override fun connect(host: String, port: Int) {
        try {
            native.connect(InetSocketAddress(host, port))
        } catch (e: java.net.UnknownHostException) {
            throw UnknownHostException(e.message!!)
        } catch (e: java.net.ConnectException) {
            throw ConnectException(e.message)
        }
    }

    override val connected: Boolean
        get() = native.isConnected && !closed

    override val closed: Boolean
        get() = native.isClosed


}