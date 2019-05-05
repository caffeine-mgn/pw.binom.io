package pw.binom.io.socket

expect class ServerSocketChannel(): NetworkChannel {
    fun bind(port: Int)
    fun accept(): SocketChannel?
}