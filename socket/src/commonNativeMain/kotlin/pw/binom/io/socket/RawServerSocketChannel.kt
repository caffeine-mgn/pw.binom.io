package pw.binom.io.socket

import pw.binom.doFreeze

actual class RawServerSocketChannel constructor(socket: RawSocketServer) : ServerSocketChannel, NetworkChannel {
    override val nsocket
        get() = server.socket.native
    override val type: Int
        get() = RAW_SOCKET_SERVER_TYPE

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

    override fun bind(address: NetworkAddress) {
        server.bind(address.host, address.port)
    }

    override fun accept(): RawSocketChannel? {
        val socket = server.accept() ?: return null
        return RawSocketChannel(socket)
    }
}