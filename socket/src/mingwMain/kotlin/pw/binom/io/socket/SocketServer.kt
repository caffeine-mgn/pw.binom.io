package pw.binom.io.socket

import platform.posix.INVALID_SOCKET
import platform.posix.accept
import pw.binom.io.Closeable
import pw.binom.io.IOException
import kotlin.native.concurrent.ensureNeverFrozen

actual class SocketServer : Closeable {
    internal val socket = Socket()

    init {
        this.ensureNeverFrozen()
    }

    override fun close() {
        socket.close()
    }

    actual fun bind(port: Int) {
        socket.bind(port)
    }

    actual fun accept(): Socket? {

        val native = accept(socket.native, null, null)
        if (native == INVALID_SOCKET)
            throw IOException("Can't accept new client")
        return Socket(native)
    }

    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}