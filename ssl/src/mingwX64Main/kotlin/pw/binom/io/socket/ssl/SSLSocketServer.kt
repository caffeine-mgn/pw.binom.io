package pw.binom.io.socket.ssl

import pw.binom.io.socket.NetworkChannel
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.RawSocketServer
import pw.binom.io.socket.SocketServer
import pw.binom.ssl.SSLContext

/*
actual class SSLSocketServer(val ctx: SSLContext, val raw: RawSocketServer) : SocketServer, NetworkChannel {
    override val nsocket: RawSocket
        get() = raw.socket
    override val type: Int
        get() = 0x010b

    constructor(ctx: SSLContext) : this(ctx, RawSocketServer())

    override fun bind(host: String, port: Int) {
        raw.bind(host, port)
    }

    override fun accept(): SSLSocket? {
        val r = raw.accept() ?: return null

        val s = SSLSocket(ctx.server(), r)
        if (!s.accepted()) {
            s.close()
            return null
        }
        return s
    }

    override fun close() {
        raw.close()
    }

}*/
