package pw.binom.io.socket

import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

expect interface Socket : Closeable {
    val input: InputStream
    val output: OutputStream

    fun connect(host: String, port: Int)
    val connected: Boolean
    val closed: Boolean
}