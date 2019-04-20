package pw.binom.io.socket

import platform.posix.SOCKET
import pw.binom.io.Closeable

actual interface Channel : Closeable {
    val native:SOCKET
}