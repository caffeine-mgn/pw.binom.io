package pw.binom.io.socket

expect open class TcpClientNetSocket : TcpClientSocket, NetSocket {
  constructor()
  fun connect(address: InetSocketAddress): ConnectStatus
}
