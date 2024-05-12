package pw.binom.io.socket

expect class TcpNetServerSocket() : TcpServerSocket, NetSocket {
  fun accept(address: MutableInetAddress? = null): TcpClientNetSocket?
  fun bind(address: InetSocketAddress): BindStatus
}
