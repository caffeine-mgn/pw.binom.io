package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

actual interface SocketChannel : Channel, Output, Input, Closeable {
    actual fun connect(host: String, port: Int)
    actual var blocking: Boolean
    actual val isConnected: Boolean
    val socket: Socket
}