package pw.binom.io.socket

import pw.binom.io.Closeable

actual interface ServerSocketChannel : Channel, Closeable {
    actual fun bind(address: NetworkAddress)
    actual fun accept(): SocketChannel?
    actual var blocking: Boolean
}