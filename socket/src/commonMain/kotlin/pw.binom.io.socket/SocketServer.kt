package pw.binom.io.socket

import pw.binom.io.Closeable

expect class SocketServer: Closeable {
    fun bind(port: Int)
    fun accept(): Socket?
}