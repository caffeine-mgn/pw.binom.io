package pw.binom.io.socket

import pw.binom.io.ByteBuffer

interface UdpNetSocket : UdpSocket, NetSocket {
    fun bind(address: InetNetworkAddress): BindStatus
    fun send(data: ByteBuffer, address: InetNetworkAddress): Int
    fun receive(data: ByteBuffer, address: MutableInetNetworkAddress?): Int
}
