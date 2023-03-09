@file:Suppress("OPT_IN_IS_NOT_ENABLED")
@file:OptIn(UnsafeNumber::class)

package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.sockaddr_un
import platform.posix.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException

actual fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus {
//    unbind(native)
    return memScoped<BindStatus> {
        val addr = alloc<sockaddr_un>()
        memset(addr.ptr, 0, sizeOf<sockaddr_un>().convert())
        addr.sun_family = AF_UNIX.convert()
        strcpy(addr.sun_path, fileName)
        unlink(fileName)
        val bindResult = bind(
            native,
            addr.ptr.getPointer(this).reinterpret(),
            sizeOf<sockaddr_un>().convert(),
        )

        if (bindResult < 0) {
            val errno = errno
            when (errno) {
                EINVAL -> return@memScoped BindStatus.ALREADY_BINDED
            }
            if (errno == EADDRINUSE || errno == 0) {
                throw BindException("Address already in use: \"$fileName\"")
            }
            throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
        }
        val listenResult = listen(native, 1000)
        if (listenResult < 0) {
//            if (errno == ESOCKTNOSUPPORT) {
//                return@memScoped
//            }
//            if (errno == EOPNOTSUPP) {
//                return@memScoped
// //                    unbind(native)
// //                    throw IOException("Can't bind socket: Operation not supported on transport endpoint")
//            }
            unbind(native)
            throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
        }
        BindStatus.OK
    }
}

actual fun internalAccept(native: RawSocket, address: MutableNetworkAddress?): RawSocket? {
    if (address == null) {
        return accept(native, null, null).takeIf { it != -1 }
    }
    val out = if (address is CommonMutableNetworkAddress) {
        address
    } else {
        CommonMutableNetworkAddress()
    }
    val newClientRaw = memScoped {
        val len = allocArray<socklen_tVar>(1)
        len[0] = 28.convert()
        val rr = accept(
            native,
            out.data.refTo(0).getPointer(this).reinterpret(),
            len,
        )
        out.size = len[0].convert()
        rr
    }

    if (out !== address) {
        address.update(host = out.host, port = out.port)
    }
    return newClientRaw
}

actual fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableNetworkAddress?): Int {
    if (data.remaining == 0) {
        return 0
    }
    return if (address == null) {
        data.ref(0) { dataPtr, remaining ->
            recvfrom(native, dataPtr, remaining.convert(), 0, null, null)
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
                        native,
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

actual fun createSocket(socket: RawSocket, server: Boolean): Socket = PosixSocket(native = socket, server = server)
