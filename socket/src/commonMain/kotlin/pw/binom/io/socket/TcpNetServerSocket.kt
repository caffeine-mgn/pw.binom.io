package pw.binom.io.socket

interface TcpNetServerSocket : TcpServerSocket, NetSocket {
    fun accept(address: MutableNetworkAddress?): TcpClientNetSocket?
    fun bind(address: NetworkAddress): BindStatus
}
