package pw.binom.io.socket.ssl

import pw.binom.io.socket.*
import pw.binom.ssl.SSLContext
import java.nio.channels.SocketChannel as JSocketChannel

/*
actual class SSLSocketFactory(val ctx: SSLContext) : SocketFactory {
    override fun createSocket(): Socket = SSLSocket(ctx)

    override fun createSocketServer(): SocketServer = SSLSocketServer(ctx)

    override fun createSocketChannel(): SocketChannel {
        val engine = ctx.ctx.createSSLEngine()
        engine.useClientMode = true
        return SSLSocketChannel(JSocketChannel.open(), engine)
    }

    override fun createSocketServerChannel(): ServerSocketChannel =
            SSLServerSocketChannel(ctx)
}*/
