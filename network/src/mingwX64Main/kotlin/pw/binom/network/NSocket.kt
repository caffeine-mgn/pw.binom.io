package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.SOCKET
import platform.linux.internal_setsockopt
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.socket
import platform.windows.*
import platform.windows.FIONBIO
import platform.windows.closesocket
import platform.windows.ioctlsocket
import platform.windows.shutdown
import pw.binom.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

private fun setBlocking(native: SOCKET, value: Boolean) {
    memScoped {
        val nonBlocking = alloc<UIntVar>()
        nonBlocking.value = if (value) 0u else 1u
        if (ioctlsocket(native, FIONBIO.convert(), nonBlocking.ptr) == -1) {
            if (GetLastError() == platform.windows.WSAENOTSOCK.toUInt()) {
                throw IOException("Can't set non blocking mode. Socket is invalid")
            }
            throw IOException("Can't set non blocking mode. Error: ${GetLastError()}")
        }
    }
}

private fun bind(native: SOCKET, address: NetworkAddress, family: Int) {
    memScoped {
        val bindResult = if (family == AF_INET6) {
            address.isAddrV6 { data ->
                platform.posix.bind(
                    native,
                    data.reinterpret(),
                    sizeOf<sockaddr_in6>().convert()
                )
            }
        } else {
            if (address.type != NetworkAddress.Type.IPV4) {
                throw IllegalArgumentException("Can't bind ipv4 to ipv6 address")
            }
            address.data.usePinned { data ->
                platform.posix.bind(
                    native,
                    data.addressOf(0).getPointer(this).reinterpret(),
                    data.get().size.convert()
                )
            }
        }

        if (bindResult < 0) {
            if (GetLastError().toInt() == platform.windows.WSAEADDRINUSE) { // 10048
                throw BindException("Address already in use: $address")
            }
            if (GetLastError().toInt() == platform.windows.WSAEACCES) { // 10013
                throw BindException("Can't bind $address: An attempt was made to access a socket in a way forbidden by its access permissions.")
            }

            if (GetLastError().toInt() == platform.windows.WSAEAFNOSUPPORT) { // 10047
                throw BindException("Can't bind to $address: Address family not supported by protocol family")
            }
            if (GetLastError().toInt() == platform.windows.WSAEFAULT) { // 10014
                throw BindException("Can't bind to $address: Bad address")
            }

            throw IOException("Bind error. errno: [$errno], GetLastError: [${GetLastError()}]")
        }
        val listenResult = platform.windows.listen(native, 1000)
        if (listenResult < 0) {
            if (GetLastError().toInt() == platform.windows.WSAEOPNOTSUPP) { // 10045
                return
            }
            throw IOException("Listen error. errno: [$errno], GetLastError: [${GetLastError()}]")
        }
    }
}
actual typealias RawSocket = Int

private fun allowIpv4(native: SOCKET) {
    memScoped {
        val flag = allocArray<IntVar>(1)
        flag[0] = 0
        val iResult = internal_setsockopt(
            native, IPPROTO_IPV6,
            IPV6_V6ONLY, flag, sizeOf<IntVar>().convert()
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

actual class NSocket(val native: SOCKET, val family: Int) : Closeable {
    actual companion object {
        actual fun serverTcp(address: NetworkAddress): NSocket {
            init_sockets()
            val domain = when (address.type) {
                NetworkAddress.Type.IPV4 -> AF_INET
                NetworkAddress.Type.IPV6 -> AF_INET6
            }
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0uL) {
                throw RuntimeException("Tcp Socket Creation")
            }
            bind(native, address, family = domain)
            if (address.type == NetworkAddress.Type.IPV6) {
                allowIpv4(native)
            }
            return NSocket(native, family = domain)
        }

        actual fun connectTcp(address: NetworkAddress, blocking: Boolean): NSocket {
            return memScoped {
                init_sockets()
                val domain = when (address.type) {
                    NetworkAddress.Type.IPV4 -> AF_INET
                    NetworkAddress.Type.IPV6 -> AF_INET6
                }
                val native = socket(domain, SOCK_STREAM, 0)
                if (native < 0uL) {
                    throw RuntimeException("Tcp Socket Creation")
                }
                setBlocking(native, blocking)
                val con = address.data.usePinned { data ->
                    platform.windows.connect(
                        native,
                        data.addressOf(0).getPointer(this).reinterpret(),
                        data.get().size.convert()
                    )
                }
                if (con < 0) {
                    if (GetLastError() == platform.windows.WSAEAFNOSUPPORT.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()} An address incompatible with the requested protocol was used.")
                    }
                    if (GetLastError() == platform.windows.WSAETIMEDOUT.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()} A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond.")
                    }
                    if (GetLastError() != platform.windows.WSAEWOULDBLOCK.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()}")
                    }
                }
                NSocket(native, family = domain)
            }
        }

        actual fun udp(): NSocket {
            init_sockets()
            val native = socket(AF_INET6, platform.windows.SOCK_DGRAM.convert(), 0)
            if (native == INVALID_SOCKET) {
                throw IOException("Can't create UDP socket. Error: ${GetLastError()}")
            }
            allowIpv4(native)
            return NSocket(native, family = AF_INET6)
        }
    }

    actual val raw: RawSocket
        get() = native.convert()

    actual val port: Int?
        get() {
            return memScoped {
                val sin = alloc<sockaddr_in6>()
                memset(sin.ptr, 0, sizeOf<sockaddr_in6>().convert())
                val addrlen = alloc<socklen_tVar>()
                addrlen.value = sizeOf<sockaddr_in6>().convert()
                val r = platform.windows.getsockname(native, sin.ptr.reinterpret(), addrlen.ptr)
                if (r == 0) {
                    ntohs(sin.sin6_port).toInt()
                } else {
                    null
                }
            }
        }

    actual fun accept(address: NetworkAddress.Mutable?): NSocket? {
        val native = if (address == null) {
            platform.windows.accept(native, null, null)
        } else {
            memScoped {
                SetLastError(0)
                val len = allocArray<IntVar>(1)
                len[0] = 28
                val rr = platform.windows.accept(
                    native,
                    address.data.refTo(0).getPointer(this).reinterpret(),
                    len
                )
                address.size = len[0]
                rr
            }
        }
        if (native == INVALID_SOCKET) {
            return null // throw IOException("Can't accept new client")
        }
        return NSocket(native = native, family = family)
    }

    actual fun send(data: ByteBuffer): Int {
        memScoped {
            val r: Int = data.ref { dataPtr, remaining ->
                send(native, dataPtr, remaining.convert(), 0).convert()
            }
            if (r < 0) {
                val error = GetLastError()
                if (error == platform.windows.WSAEWOULDBLOCK.convert<DWORD>())
                    return 0

                if (error == platform.windows.WSAECONNABORTED.convert<DWORD>() || error == platform.windows.WSAENOTSOCK.convert<DWORD>() || error == platform.windows.WSAECONNRESET.convert<DWORD>()) {
                    throw SocketClosedException()
                }
                throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {

        val r: Int = data.ref { dataPtr, remaining ->
            platform.windows.recv(native, dataPtr, remaining.convert(), 0).convert()
        }
        if (r < 0) {
            val error = GetLastError()
            if (error == platform.windows.WSAEWOULDBLOCK.convert<DWORD>())
                return 0
            throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
        }
        if (r > 0) {
            data.position += r
        }
        return r
    }

    private fun nativeClose() {
        shutdown(native, SD_SEND)
        closesocket(native)
    }

    override fun close() {
        nativeClose()
    }

    actual fun setBlocking(value: Boolean) {
        setBlocking(native, value)
    }

    actual fun connect(address: NetworkAddress) {
        memScoped {
            address.data.usePinned { data ->
                val con = platform.windows.connect(
                    native,
                    data.addressOf(0).getPointer(this).reinterpret(),
                    data.get().size.convert()
                )
                if (con < 0) {
                    if (GetLastError() == platform.windows.WSAEAFNOSUPPORT.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()} An address incompatible with the requested protocol was used.")
                    }
                    if (GetLastError() == platform.windows.WSAETIMEDOUT.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()} A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond.")
                    }
                    if (GetLastError() != platform.windows.WSAEWOULDBLOCK.toUInt()) {
                        throw SocketConnectException("Can't connect to $address. Error: ${GetLastError()}")
                    }
                }
            }
        }
    }

    actual fun bind(address: NetworkAddress) {
        bind(native, address, family)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        memScoped {
            val rr = data.ref { dataPtr, remaining ->
                address.data.usePinned { addressPtr ->
                    if (family == AF_INET6) {

                        address.isAddrV6 { addr ->
                            sendto(
                                native, dataPtr.getPointer(this), remaining.convert(),
                                0,
                                addr.reinterpret(), sizeOf<sockaddr_in6>().convert()
                            )
                        }
                    } else {
                        sendto(
                            native, dataPtr.getPointer(this), remaining.convert(),
                            0,
                            addressPtr.addressOf(0).getPointer(this).reinterpret(), address.size.convert()
                        )
                    }
                }
            }
            if (rr == SOCKET_ERROR) {
                if (GetLastError().toInt() == platform.windows.WSAEFAULT) { // 10014
                    throw IOException("The system detected an invalid pointer address in attempting to use a pointer argument in a call.")
                }
                if (GetLastError().toInt() == platform.windows.WSAEWOULDBLOCK) { // 10035
                    return 0
                }
                throw IOException("Can't send data. Error: $errno  ${GetLastError()}")
            }

            data.position += rr.toInt()
            rr
        }

    actual fun recv(
        data: ByteBuffer,
        address: NetworkAddress.Mutable?
    ): Int {
        val gotBytes = if (address == null) {

            val rr = data.ref { dataPtr, remaining ->
                platform.windows.recvfrom(
                    native,
                    dataPtr,
                    remaining.convert(),
                    0,
                    null,
                    null
                )
            }
            if (rr == SOCKET_ERROR) {
                if (GetLastError().convert<UInt>() == platform.windows.WSAEWOULDBLOCK.convert<UInt>()) {
                    return 0
                }
                if (GetLastError().convert<UInt>() == platform.windows.WSAECONNRESET.convert<UInt>()) { // 10054
                    throw IOException("Connection reset by peer.")
                }

                throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
            }
            rr
        } else {
            memScoped {
                SetLastError(0)
                val len = allocArray<IntVar>(1)
                len[0] = 28

                val rr = data.ref { dataPtr, remaining ->
                    address.data.usePinned { addressPtr ->
                        platform.windows.recvfrom(
                            native, dataPtr.getPointer(this), remaining.convert(), 0,
                            addressPtr.addressOf(0).getPointer(this).reinterpret<sockaddr>(),
                            len
                        )
                    }
                }

                if (rr == SOCKET_ERROR) {
                    if (GetLastError().convert<UInt>() == platform.windows.WSAEWOULDBLOCK.convert<UInt>()) {
                        return 0
                    }
                    if (GetLastError().convert<UInt>() == platform.windows.WSAECONNRESET.convert<UInt>()) { // 10054
                        throw IOException("Connection reset by peer.")
                    }
                    throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
                }
                address.size = len[0]
                address.hashCodeDone = false
                rr
            }
        }
        data.position += gotBytes.toInt()
        return gotBytes
    }
}
