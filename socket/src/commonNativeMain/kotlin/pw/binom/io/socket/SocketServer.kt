package pw.binom.io.socket

import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.neverFreeze

actual class SocketServer : Closeable {
    internal val socket = Socket()

    init {
        doFreeze()
    }

    override fun close() {
        socket.close()
    }

    actual fun bind(port: Int) {
        socket.bind(port)
    }

    actual fun accept(): Socket? {
        val r = Socket(acceptSocket(socket.native))
        r.internalConnected()
        return r
    }

    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}