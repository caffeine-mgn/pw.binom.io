package pw.binom.io.socket

internal actual fun createNetworkAddress(host: String, port: Int): NetworkAddress {
    val ret = createMutableNetworkAddress()
    ret.update(
        host = host,
        port = port
    )
    return ret
}

internal actual fun createMutableNetworkAddress(): MutableNetworkAddress = PosixMutableNetworkAddress()
