package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

actual interface Socket : Closeable, Output, Input {
    actual val input: InputStream
    actual val output: OutputStream
    actual fun connect(host: String, port: Int)
    actual val connected: Boolean
    actual val closed: Boolean
}