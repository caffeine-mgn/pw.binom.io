package pw.binom.io.socket

import pw.binom.io.Closeable

actual interface Socket : Closeable {
    actual var blocking: Boolean
    actual val port: Int?
}
