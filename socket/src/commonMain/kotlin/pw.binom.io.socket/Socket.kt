package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

expect interface Socket : Closeable, Output, Input {
    fun connect(host: String, port: Int)
    val connected: Boolean
    val closed: Boolean
}