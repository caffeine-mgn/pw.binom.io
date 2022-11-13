package pw.binom.network

import pw.binom.io.ByteBuffer

expect class UdpSocketChannel() : NetworkChannel {
    fun setBlocking(value: Boolean)
    fun send(data: ByteBuffer, address: NetworkAddress): Int
    fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int
    fun bind(address: NetworkAddress)
    val port: Int?
}
