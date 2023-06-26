@file:JvmName("JvmInternal")

package pw.binom.io.socket

internal actual fun createNetworkAddress(host: String, port: Int): InetNetworkAddress {
    val ret = createMutableNetworkAddress()
    ret.update(
        host = host,
        port = port
    )
    return ret
}

internal actual fun createMutableNetworkAddress(): MutableInetNetworkAddress = JvmMutableInetNetworkAddress()
