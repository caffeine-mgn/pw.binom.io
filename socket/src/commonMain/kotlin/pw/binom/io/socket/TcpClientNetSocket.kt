package pw.binom.io.socket

interface TcpClientNetSocket : TcpClientSocket, NetSocket {
    fun connect(address: NetworkAddress): ConnectStatus
}
