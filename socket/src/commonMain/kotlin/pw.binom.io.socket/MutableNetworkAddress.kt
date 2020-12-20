package pw.binom.io.socket

expect class MutableNetworkAddress : NetworkAddress {
    constructor()
    fun reset(type: NetworkAddress.Type, host: String, port: Int)
    fun reset(host: String, port: Int)
}