package pw.binom.io.socket

import kotlin.native.concurrent.ensureNeverFrozen

actual class ServerSocketChannel actual constructor(): Channel {

    init {
        this.ensureNeverFrozen()
    }

    override fun close() {
        socket.close()
    }

    internal val socket = SocketServer()

    actual fun bind(port: Int) {
        socket.bind(port)
    }

    actual fun accept(): SocketChannel? {
        val socket = socket.accept() ?: return null
        return SocketChannel(socket)
    }

    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking=value
        }
}