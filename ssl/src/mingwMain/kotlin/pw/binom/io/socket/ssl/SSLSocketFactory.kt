package pw.binom.io.socket.ssl

import pw.binom.io.socket.*
import pw.binom.ssl.SSLContext

actual class SSLSocketFactory internal constructor(val ctx: SSLContext) : SocketFactory {
    override fun createSocket(): Socket =
            SSLSocket(ctx)

    override fun createSocketServer(): SocketServer =
            SSLSocketServer(ctx)

    override fun createSocketChannel(): SocketChannel = SSLSocketChannel(SSLSocket(ctx))

    override fun createSocketServerChannel(): ServerSocketChannel = SSLServerSocketChannel(SSLSocketServer(ctx))

//    actual override fun createSocket(): Socket =
//            SSLSocket(ctx.getClientSessionContext())
//
//    actual override fun createSocketServer(): SocketServer = SSLSocketServer(ctx.getServerSessionContext())
}