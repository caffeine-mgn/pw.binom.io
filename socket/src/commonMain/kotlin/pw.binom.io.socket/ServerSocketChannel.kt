package pw.binom.io.socket

import pw.binom.io.Closeable

expect interface ServerSocketChannel : Channel, Closeable {
    fun bind(address:NetworkAddress)
    fun accept(): SocketChannel?
    var blocking: Boolean
}