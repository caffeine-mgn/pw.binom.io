package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

expect interface SocketChannel : Channel, Output, Input, Closeable {
    fun connect(host: String, port: Int)
    var blocking: Boolean
    val isConnected: Boolean
}