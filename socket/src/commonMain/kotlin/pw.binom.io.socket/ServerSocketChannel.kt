package pw.binom.io.socket

expect class ServerSocketChannel() : NetworkChannel {
    fun bind(host: String = "0.0.0.0", port: Int)
    fun accept(): SocketChannel?
    var blocking: Boolean

}