package pw.binom.io.socket

actual class ServerSocketChannel actual constructor(): Channel {
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