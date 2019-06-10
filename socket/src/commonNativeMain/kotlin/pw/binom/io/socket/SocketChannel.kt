package pw.binom.io.socket

import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

actual interface SocketChannel : Channel, OutputStream, InputStream, Closeable {
    actual fun connect(host: String, port: Int)
    actual var blocking: Boolean
    actual val isConnected: Boolean
    val socket: Socket
}