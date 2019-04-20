package pw.binom.io.socket

expect class ServerSocketChannel(): Channel {
    fun bind(port: Int)
    fun accept(): SocketChannel?
    var blocking:Boolean
}