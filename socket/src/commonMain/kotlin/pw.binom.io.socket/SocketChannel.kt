package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

expect interface SocketChannel : Channel, Output, Input, Closeable {
    fun connect(address: NetworkAddress)
    var blocking: Boolean
    val isConnected: Boolean
}