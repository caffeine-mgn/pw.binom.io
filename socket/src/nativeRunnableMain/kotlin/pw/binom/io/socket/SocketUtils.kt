package pw.binom.io.socket

import platform.common.internal_set_allow_ipv6
import platform.common.internal_set_socket_blocked_mode
import platform.common.internal_unbind
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException

expect fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus
fun unbind(native: RawSocket) {
    internal_unbind(native)
}

fun setBlocking(native: Int, value: Boolean) {
    if (internal_set_socket_blocked_mode(native, if (value) 1 else 0) <= 0) {
        throw IOException("Can't change blocking mode")
    }
}

fun allowIpv4(native: RawSocket) {
    if (internal_set_allow_ipv6(native) <= 0) {
        throw IOException("Can't allow ipv6 connection for UDP socket.")
    }
}

expect fun internalAccept(native: RawSocket, address: MutableNetworkAddress?): RawSocket?

expect fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableNetworkAddress?): Int

internal expect fun createSocket(socket: RawSocket, server: Boolean): Socket

internal actual fun createNetworkAddress(host: String, port: Int): NetworkAddress {
    val ret = createMutableNetworkAddress()
    ret.update(
        host = host,
        port = port
    )
    return ret
}

internal actual fun createMutableNetworkAddress(): MutableNetworkAddress = CommonMutableNetworkAddress()
