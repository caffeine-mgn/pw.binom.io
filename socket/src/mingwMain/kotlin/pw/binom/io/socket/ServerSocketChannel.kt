package pw.binom.io.socket

import platform.posix.SOCKET
import kotlin.native.concurrent.ensureNeverFrozen

actual class ServerSocketChannel actual constructor() : Channel {

    init {
        this.ensureNeverFrozen()
    }

    override val native: SOCKET
        get() = socket.socket.native

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
            socket.blocking = value
        }
}