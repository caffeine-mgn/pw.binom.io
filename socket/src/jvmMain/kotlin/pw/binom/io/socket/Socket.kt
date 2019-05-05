package pw.binom.io.socket

import pw.binom.io.*
import java.net.InetSocketAddress
import java.net.Socket as JSocket

actual class Socket constructor(val native: JSocket) : Closeable, InputStream, OutputStream {
    override fun flush() {
        //NOP
    }

    actual constructor() : this(JSocket())

    override fun close() {
        native.close()
    }

    actual fun connect(host: String, port: Int) {
        try {
            native.connect(InetSocketAddress(host, port))
        } catch (e: java.net.UnknownHostException) {
            throw UnknownHostException(e.message!!)
        } catch (e: java.net.ConnectException) {
            throw ConnectException(e.message)
        }
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int =
            try {
                val reded = native.getInputStream().read(data, offset, length)
                if (reded == -1) {
                    close()
                    throw SocketClosedException()
                }
                reded
            } catch (e: java.io.IOException) {
                throw IOException(e.message)
            }

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

    actual val connected: Boolean
        get() = native.isConnected && !closed

    actual val closed: Boolean
        get() = native.isClosed


}