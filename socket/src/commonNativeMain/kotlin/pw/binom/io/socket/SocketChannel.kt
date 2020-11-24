package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

actual interface SocketChannel : Channel, Output, Input, Closeable {
    actual fun connect(address: NetworkAddress)
    actual var blocking: Boolean
    actual val isConnected: Boolean
    val socket: Socket
}