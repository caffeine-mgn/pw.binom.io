package pw.binom.io.socket

import pw.binom.io.InputStream
import pw.binom.io.OutputStream

expect class SocketChannel() : NetworkChannel, OutputStream, InputStream {
    fun connect(host: String, port: Int)
    var blocking: Boolean
    val isConnected: Boolean
}