package pw.binom.io.socket

import pw.binom.doFreeze

actual class RawSocketServer : SocketServer {
    val socket = RawSocket()

    init {
        doFreeze()
    }

    override fun close() {
        socket.close()
    }

    override fun bind(host: String, port: Int) {
        socket.bind(host, port)
    }

    override fun accept(): RawSocket? {
        val r = RawSocket(acceptSocket(socket.native))
        r.internalConnected()
        return r
    }

    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}