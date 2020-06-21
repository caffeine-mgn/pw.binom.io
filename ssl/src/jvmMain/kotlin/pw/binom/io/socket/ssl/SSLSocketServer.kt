package pw.binom.io.socket.ssl

import pw.binom.io.socket.NetworkChannel
import pw.binom.io.socket.RawSocketServer
import pw.binom.io.socket.Socket
import pw.binom.io.socket.SocketServer
import pw.binom.ssl.SSLContext

/*
actual class SSLSocketServer(ctx: SSLContext) : SocketServer, NetworkChannel {

    val raw = RawSocketServer(ctx.ctx.serverSocketFactory.createServerSocket())

    override fun bind(host: String, port: Int) {
        raw.bind(host, port)
    }

    override fun accept(): Socket? = SSLSocket(raw.accept()!!.native)

    override fun close() {
        raw.close()
    }
}*/
