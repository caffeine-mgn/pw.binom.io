package pw.binom.io.socket

import java.net.InetSocketAddress
import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream
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
        native.connect(InetSocketAddress(host, port))
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int =
            native.getInputStream().read(data, offset, length)

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        native.getOutputStream().write(data, offset, length)
        return length
    }


    actual companion object {
        actual fun startup() {
        }

        actual fun shutdown() {
        }
    }

    actual val connected: Boolean
        get() = native.isConnected

}