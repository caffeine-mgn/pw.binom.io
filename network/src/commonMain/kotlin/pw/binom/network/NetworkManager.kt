package pw.binom.network

interface NetworkManager {
    fun attach(channel: UdpSocketChannel): UdpConnection
    fun attach(channel: TcpClientSocketChannel): TcpConnection
    fun attach(channel: TcpServerSocketChannel): TcpServerConnection
}

fun NetworkCoroutineDispatcher.bindTcp(address: NetworkAddress): TcpServerConnection {
    val channel = TcpServerSocketChannel()
    channel.bind(address)
    return attach(channel)
}

fun NetworkCoroutineDispatcher.bindUdp(address: NetworkAddress): UdpConnection {
    val channel = UdpSocketChannel()
    channel.bind(address)
    return attach(channel)
}