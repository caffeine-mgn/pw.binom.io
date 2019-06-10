package pw.binom.io.socket

import pw.binom.io.Closeable

expect interface ServerSocketChannel : Channel, Closeable {
    fun bind(host: String = "0.0.0.0", port: Int)
    fun accept(): SocketChannel?
    var blocking: Boolean
}