package pw.binom.io.socket

interface SocketFactory {
    fun createSocket(): Socket
    fun createSocketServer(): SocketServer

    fun createSocketChannel(): SocketChannel
    fun createSocketServerChannel(): ServerSocketChannel

    companion object
}