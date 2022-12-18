package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.INVALID_SOCKET
import platform.windows.SetLastError
import platform.windows.accept

actual fun internalAccept(native: RawSocket, address: MutableNetworkAddress?): RawSocket? {
    val native = if (address == null) {
        platform.windows.accept(native.convert(), null, null)
    } else {

        val out = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress()
        }
        val rr2 = memScoped {
            SetLastError(0)
            val len = allocArray<IntVar>(1)
            len[0] = 28
            val rr = out.addr { addr ->
                accept(
                    native.convert(),
                    addr.reinterpret(),
                    len
                )
            }
            out.size = len[0]
            rr
        }
        if (out !== address) {
            address.update(out.host, out.port)
        }
        rr2
    }
    if (native == INVALID_SOCKET) {
        return null // throw IOException("Can't accept new client")
    }
    return native.convert()
}

// actual fun allowIpv4(native: RawSocket) {
//    memScoped {
//        val flag = allocArray<IntVar>(1)
//        flag[0] = 0
//        val iResult = internal_setsockopt(
//            native.convert(),
//            IPPROTO_IPV6,
//            IPV6_V6ONLY,
//            flag,
//            sizeOf<IntVar>().convert()
//        )
//        if (iResult == SOCKET_ERROR) {
//            closesocket(native)
//            throw IOException("Can't allow ipv6 connection for UDP socket. Error: ${GetLastError()}")
//        }
//    }
// }

// actual fun setBlocking(native: RawSocket, value: Boolean) {
//    memScoped {
//        val nonBlocking = alloc<UIntVar>()
//        nonBlocking.value = if (value) 0u else 1u
//        if (ioctlsocket(native, FIONBIO.convert(), nonBlocking.ptr) == -1) {
//            if (GetLastError() == platform.windows.WSAENOTSOCK.toUInt()) {
//                return@memScoped false
// //                throw IOException("Can't set non blocking mode. Socket is invalid")
//            }
//            throw IOException("Can't set non blocking mode. Error: ${GetLastError()}")
//        }
//        true
//    }
// }
