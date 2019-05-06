package pw.binom.io.socket

import platform.posix.accept
import pw.binom.io.Closeable
import kotlin.native.concurrent.ensureNeverFrozen

actual class SocketServer : Closeable {

    init {
        this.ensureNeverFrozen()
    }

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
        s.internalConnected()
        return s
    }

    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}