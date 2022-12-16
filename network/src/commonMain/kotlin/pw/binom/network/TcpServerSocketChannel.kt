package pw.binom.network

import pw.binom.io.Closeable

expect class TcpServerSocketChannel() : Closeable, NetworkChannel {
    val port: Int?
    fun setBlocking(value: Boolean)
    fun accept(address: NetworkAddressOld.Mutable? = null): TcpClientSocketChannel?
    fun bind(address: NetworkAddressOld)
    fun bind(fileName: String)
}
