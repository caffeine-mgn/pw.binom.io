package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.SOCKET
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

actual class NSocket(var native: SOCKET, var tcp: Boolean) : Closeable {
    private var type = NetworkAddress.Type.IPV4

    actual companion object {
        actual fun tcp(): NSocket {
            init_sockets()
            val native = socket(AF_INET, SOCK_STREAM, 0)
            if (native < 0uL) {
                throw RuntimeException("Tcp Socket Creation")
            }
            return NSocket(native, true)
        }

        actual fun udp(): NSocket {
            init_sockets()
            val native = socket(AF_INET, platform.windows.SOCK_DGRAM.convert(), 0)
            if (native < 0uL) {
                throw RuntimeException("Datagram Socket Creation")
            }
            return NSocket(native, false)
        }
    }

    actual val port: Int?
        get() {
            return memScoped {
                val sin = alloc<sockaddr_in>()
                memset(sin.ptr, 0, sizeOf<sockaddr_in>().convert())
                val addrlen = alloc<socklen_tVar>()
                addrlen.value = sizeOf<sockaddr_in>().convert()
                val r = platform.windows.getsockname(native, sin.ptr.reinterpret(), addrlen.ptr)
                if (r == 0) {
                    ntohs(sin.sin_port).toInt()
                } else {
                    // println("getsockname=$c, errno=${errno},GetLastError()=${GetLastError()}")
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
        return NSocket(native = native, tcp = tcp)
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
        memScoped {
            val nonBlocking = alloc<UIntVar>()
            nonBlocking.value = if (value) 0u else 1u
            if (ioctlsocket(native, FIONBIO.convert(), nonBlocking.ptr) == -1)
                throw IOException("ioctlsocket() error. ErrNo: ${GetLastError()}")
        }
    }

    private fun prepareSocket(type: NetworkAddress.Type, tcp: Boolean) {
        if (this.type == type && this.tcp == tcp) {
            return
        }
        nativeClose()
        val nativeAddressType = when (type) {
            NetworkAddress.Type.IPV4 -> AF_INET
            NetworkAddress.Type.IPV6 -> AF_INET6
        }
        val nativeType = when (tcp) {
            true -> SOCK_STREAM
            false -> platform.posix.SOCK_DGRAM
        }
        native = socket(nativeAddressType, nativeType, 0)
        if (native < 0uL) {
            throw RuntimeException("Tcp Socket Creation ipv4")
        }
        this.type = type
    }

    actual fun connect(address: NetworkAddress) {
        prepareSocket(type = address.type, tcp = true)
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
        prepareSocket(type = address.type, tcp = tcp)
        memScoped {
            val bindResult = platform.posix.bind(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (bindResult < 0) {
                if (GetLastError().toInt() == platform.windows.WSAEADDRINUSE) { // 10048
                    throw BindException("Address already in use: ${address.host}:${address.port}")
                }
                if (GetLastError().toInt() == platform.windows.WSAEACCES) { // 10013
                    throw BindException("Can't bind ${address.host}:${address.port}: An attempt was made to access a socket in a way forbidden by its access permissions.")
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

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        memScoped {
            val rr = data.ref { dataPtr, remaining ->
                address.data.usePinned { addressPtr ->
                    sendto(
                        native, dataPtr.getPointer(this), remaining.convert(),
                        0,
                        addressPtr.addressOf(0).getPointer(this).reinterpret(), address.size.convert()
                    )
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
