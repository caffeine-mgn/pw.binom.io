package pw.binom.network

import pw.binom.io.Channel

expect class TcpClientSocketChannel : Channel {
    constructor()
    fun setBlocking(value: Boolean)
    fun connect(address: NetworkAddress)
}