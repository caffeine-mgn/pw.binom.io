package pw.binom.io.socket

import platform.posix.accept
import pw.binom.io.Closeable

actual class SocketServer : Closeable {

    internal val socket = Socket()

    override fun close() {
        socket.close()
    }

    actual fun bind(port: Int) {
        socket.bind(port)
    }

    actual fun accept(): Socket? {
        val native = accept(socket.native, null, null)
        if (native == -1)
            return null
        val s = Socket(native)
        s.setConnected()
        return s
    }

    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}