package pw.binom.io.socket

import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

expect class Socket() : Closeable, InputStream, OutputStream {
    fun connect(host: String, port: Int)
    val connected: Boolean

    companion object {
        fun startup()
        fun shutdown()
    }
}