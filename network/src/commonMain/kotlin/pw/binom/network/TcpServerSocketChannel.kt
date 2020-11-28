package pw.binom.network

import pw.binom.io.Channel
import pw.binom.io.Closeable

expect class TcpServerSocketChannel : Closeable {
    fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel?
    fun bind(address: NetworkAddress)
}