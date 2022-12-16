package pw.binom.network

import pw.binom.io.ByteBuffer

expect class UdpSocketChannel() : NetworkChannel {
    fun setBlocking(value: Boolean)
    fun send(data: ByteBuffer, address: NetworkAddressOld): Int
    fun recv(data: ByteBuffer, address: NetworkAddressOld.Mutable?): Int
    fun bind(address: NetworkAddressOld)
    val port: Int?
}
