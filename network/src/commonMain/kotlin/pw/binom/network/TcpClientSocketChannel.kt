package pw.binom.network

import pw.binom.io.Channel

expect class TcpClientSocketChannel : Channel, NetworkChannel {
    constructor()

    fun setBlocking(value: Boolean)
    fun connect(address: NetworkAddressOld)
    fun connect(fileName: String)
}
