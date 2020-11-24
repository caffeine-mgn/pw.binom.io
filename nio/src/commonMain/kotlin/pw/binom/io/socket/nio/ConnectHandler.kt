package pw.binom.io.socket.nio

fun interface ConnectHandler{
    fun connected(connection:TcpConnectionRaw)
}