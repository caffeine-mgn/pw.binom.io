package pw.binom.io.socket

interface TcpNetServerSocket : TcpServerSocket, NetSocket {
  fun accept(address: MutableInetNetworkAddress?): TcpClientNetSocket?
  fun bind(address: InetNetworkAddress): BindStatus
}
