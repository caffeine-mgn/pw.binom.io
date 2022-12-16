package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.io.IOException

actual fun setBlocking(native: RawSocket, value: Boolean) {
    val flags = fcntl(native, F_GETFL, 0)
    val newFlags = if (value) {
        flags xor O_NONBLOCK
    } else {
        flags or O_NONBLOCK
    }

    if (0 != fcntl(native, F_SETFL, newFlags)) {
        throw IOException()
    }
}

actual fun allowIpv4(native: RawSocket) {
    memScoped {
        val flag = allocArray<IntVar>(1)
        flag[0] = 0
        val iResult = setsockopt(
            native, IPPROTO_IPV6, IPV6_V6ONLY, flag, sizeOf<IntVar>().convert()
        )
        if (iResult == -1) {
            close(native)
            throw IOException("Can't allow ipv6 connection for UDP socket. Error: $errno")
        }
    }
}
