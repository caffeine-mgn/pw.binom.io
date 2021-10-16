package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

expect class UdpSocketChannel() : Closeable {
    fun send(data: ByteBuffer, address: NetworkAddress): Int
    fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int
    fun bind(address: NetworkAddress)
    val port:Int?
}