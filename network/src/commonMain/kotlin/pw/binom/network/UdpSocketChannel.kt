package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

expect class UdpSocketChannel() : Closeable {
    fun setBlocking(value: Boolean)
    fun send(data: ByteBuffer, address: NetworkAddress): Int
    fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int
    fun bind(address: NetworkAddress)
    val port: Int?
}
