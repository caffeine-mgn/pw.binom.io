package pw.binom.io.socket

import pw.binom.doFreeze

actual class RawServerSocketChannel constructor(socket: RawSocketServer) : ServerSocketChannel, NetworkChannel {
    override val nsocket: RawSocket
        get() = server.socket

    actual constructor() : this(RawSocketServer())

    private val server = socket

    override var blocking: Boolean
        get() = server.blocking
        set(value) {
            server.blocking = value
        }

    init {
        doFreeze()
    }

    override fun close() {
        server.close()
    }

    override fun bind(host: String, port: Int) {
        server.bind(host, port)
    }

    override fun accept(): RawSocketChannel? {
        val socket = server.accept() ?: return null
        return RawSocketChannel(socket)
    }
}