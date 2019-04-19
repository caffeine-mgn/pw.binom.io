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
            throw ConnectException(host = host, port = port)
        }
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int =
            native.getInputStream().read(data, offset, length)

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        native.getOutputStream().write(data, offset, length)
        return length
    }

    actual val connected: Boolean
        get() = native.isConnected

}