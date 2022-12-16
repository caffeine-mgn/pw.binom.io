package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.internal_setsockopt
import platform.posix.IPPROTO_IPV6
import platform.posix.SOCKET_ERROR
import platform.windows.GetLastError
import platform.windows.IPV6_V6ONLY
import platform.windows.closesocket
import pw.binom.io.IOException

actual fun allowIpv4(native: RawSocket) {
    memScoped {
        val flag = allocArray<IntVar>(1)
        flag[0] = 0
        val iResult = internal_setsockopt(
            native.convert(),
            IPPROTO_IPV6,
            IPV6_V6ONLY,
            flag,
            sizeOf<IntVar>().convert()
        )
        val flag2 = allocArray<IntVar>(1)
        flag[0] = 3
        flag2[0] = sizeOf<IntVar>().convert()
        val vv = platform.windows.getsockopt(native, IPPROTO_IPV6, IPV6_V6ONLY, flag.reinterpret(), flag2)
        if (iResult == SOCKET_ERROR) {
            closesocket(native)
            throw IOException("Can't allow ipv6 connection for UDP socket. Error: ${GetLastError()}")
        }
    }
}
