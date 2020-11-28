package pw.binom.io.socket

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

actual interface Socket : Closeable, Output, Input {

    actual fun connect(host: String, port: Int)
    actual val connected: Boolean
    actual val closed: Boolean
    val native: NativeSocketHolder
}