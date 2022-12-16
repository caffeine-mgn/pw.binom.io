package pw.binom.io.socket

interface TcpClientUnixSocket : TcpClientSocket {
    fun connect(path: String): ConnectStatus
}
