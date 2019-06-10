package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual interface ServerSocketChannel : Channel, Closeable {
    actual fun bind(host: String, port: Int)
    actual fun accept(): SocketChannel?
    actual var blocking: Boolean
    val native: JServerSocketChannel
}