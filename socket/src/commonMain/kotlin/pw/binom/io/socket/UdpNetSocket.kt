package pw.binom.io.socket

import pw.binom.io.ByteBuffer

interface UdpNetSocket : UdpSocket, NetSocket {
    fun bind(address: NetworkAddress): BindStatus
    fun send(data: ByteBuffer, address: NetworkAddress): Int
    fun receive(data: ByteBuffer, address: MutableNetworkAddress?): Int
}
