package pw.binom.io.socket

object RawSocketFactory : SocketFactory {
    override fun createSocketChannel(): SocketChannel =
            RawSocketChannel()

    override fun createSocketServerChannel(): ServerSocketChannel =
            RawServerSocketChannel()

    override fun createSocket() = RawSocket()

    override fun createSocketServer() = RawSocketServer()

}

val SocketFactory.Companion.rawSocketFactory: RawSocketFactory
    get() = RawSocketFactory