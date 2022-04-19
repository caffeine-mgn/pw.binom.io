package pw.binom.network

import pw.binom.io.Closeable

expect class TcpServerSocketChannel() : Closeable {
    val port: Int?
    fun setBlocking(value: Boolean)
    fun accept(address: NetworkAddress.Mutable? = null): TcpClientSocketChannel?
    fun bind(address: NetworkAddress)
}
