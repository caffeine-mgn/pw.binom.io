package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.socklen_tVar
import platform.posix.INVALID_SOCKET
import platform.posix.recvfrom
import platform.windows.SetLastError
import platform.windows.accept
import platform.windows.sockaddr_in6
import pw.binom.io.ByteBuffer

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
                    len,
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

internal actual fun createSocket(socket: RawSocket, server: Boolean): Socket =
    MingwSocket(native = socket, server = server)

actual fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus {
    throwUnixSocketNotSupported()
}

actual fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableNetworkAddress?): Int {
//    if (data.remaining == 0) {
//        return 0
//    }
    return if (address == null) {
        data.ref(0) { dataPtr, remaining ->
            recvfrom(native.convert(), dataPtr, remaining.convert(), 0, null, null)
        }.toInt()
    } else {
        val netAddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        val readSize = netAddress.addr { addrPtr ->
            data.ref(0) { dataPtr, remaining ->
                memScoped {
                    val len = allocArray<socklen_tVar>(1)
                    len[0] = sizeOf<sockaddr_in6>().convert()
                    val r = recvfrom(
                        native.convert(),
                        dataPtr,
                        remaining.convert(),
                        0,
                        addrPtr.reinterpret(),
                        len,
                    )
                    if (r >= 0) {
                        netAddress.size = len[0].convert()
                    }
                    r
                }
            }.toInt()
        }
        if (readSize >= 0 && netAddress !== address) {
            address.update(netAddress.host, netAddress.port)
        }
        readSize
    }
}
