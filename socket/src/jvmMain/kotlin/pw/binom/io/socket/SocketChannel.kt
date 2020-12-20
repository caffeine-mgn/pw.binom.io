package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable
import java.nio.channels.SocketChannel as JSocketChannel

actual interface SocketChannel : Channel, Output, Input, Closeable {
    actual fun connect(address: NetworkAddress)
    actual var blocking: Boolean
    actual val isConnected: Boolean
    val native: JSocketChannel
}