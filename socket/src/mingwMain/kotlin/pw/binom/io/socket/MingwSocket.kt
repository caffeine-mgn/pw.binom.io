package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.SOCKET_ERROR
import platform.posix.errno
import platform.windows.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException

class MingwSocket(
    native: RawSocket,
    server: Boolean,
) : AbstractSocket(native = native, server = server) {
    override fun connect(address: NetworkAddress): ConnectStatus {
        val netaddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        return memScoped {
            netaddress.getAsIpV6 { netdata ->
                val con = platform.windows.connect(
                    native.convert(),
                    netdata.reinterpret(),
                    sizeOf<sockaddr_in6>().convert(),
                )
                if (con < 0) {
                    if (GetLastError() == platform.windows.WSAEWOULDBLOCK.toUInt()) {
                        return@getAsIpV6 ConnectStatus.IN_PROGRESS
                    }
                    if (GetLastError() == platform.windows.WSAEAFNOSUPPORT.toUInt()) {
                        throw IOException("Can't connect to $address. Error: ${GetLastError()} An address incompatible with the requested protocol was used.")
                    }
                    if (GetLastError() == platform.windows.WSAETIMEDOUT.toUInt()) {
                        throw IOException("Can't connect to $address. Error: ${GetLastError()} A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond.")
                    }
                }
                ConnectStatus.OK
            }
        }
    }

    override fun send(data: ByteBuffer): Int {
        if (closed) {
            return -1
        }
        memScoped {
            val r: Int = data.ref { dataPtr, remaining ->
                platform.posix.send(native.convert(), dataPtr, remaining.convert(), 0).convert()
            } ?: 0
            if (r < 0) {
                val error = GetLastError()
                if (error == WSAEWOULDBLOCK.convert<DWORD>()) {
                    return 0
                }

                if (error == WSAECONNABORTED.convert<DWORD>() || error == WSAENOTSOCK.convert<DWORD>() || error == WSAECONNRESET.convert<DWORD>()) {
                    return -1
                }
                throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
            }
            data.position += r
            return r
        }
    }

    override fun receive(data: ByteBuffer): Int {
        if (closed) {
            return -1
        }
        val r: Int = data.ref { dataPtr, remaining ->
            platform.windows.recv(native.convert(), dataPtr, remaining.convert(), 0).convert()
        } ?: 0
        if (r == 0) {
            closed = true
            nativeClose()
            return -1
        }
        if (r < 0) {
            val error = GetLastError()
            if (error == platform.windows.WSAEWOULDBLOCK.convert<DWORD>()) {
                return 0
            }
            return -1
//            throw IOException("Error on reading data from network. read: [$r], error: [${GetLastError()}, $errno]")
        }
        if (r > 0) {
            data.position += r
        }
        return r
    }

    override fun bind(address: NetworkAddress): BindStatus {
        val networkAddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        memScoped {
            val bindResult = networkAddress.getAsIpV6 { data ->
                platform.posix.bind(
                    native.convert(),
                    data.reinterpret(),
                    networkAddress.size.convert()
                )
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
            val listenResult = platform.windows.listen(native.convert(), 1000)
            if (listenResult < 0) {
                if (GetLastError().toInt() == platform.windows.WSAEOPNOTSUPP) { // 10045
                    return BindStatus.OK // UDP not supported listen. Ignore
                }
                throw IOException("Listen error. errno: [$errno], GetLastError: [${GetLastError()}]")
            }
        }
        return BindStatus.OK
    }

    override fun receive(data: ByteBuffer, address: MutableNetworkAddress?): Int {
        val gotBytes = if (address == null) {
            val rr = data.ref { dataPtr, remaining ->
                platform.windows.recvfrom(
                    native.convert(),
                    dataPtr,
                    remaining.convert(),
                    0,
                    null,
                    null
                )
            } ?: 0
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
                val netaddress = if (address is CommonMutableNetworkAddress) {
                    address
                } else {
                    CommonMutableNetworkAddress(address)
                }
                SetLastError(0)
                val len = allocArray<IntVar>(1)
                len[0] = 28

                val rr = data.ref { dataPtr, remaining ->
                    netaddress.addr { addressPtr ->
                        platform.windows.recvfrom(
                            native.convert(),
                            dataPtr.getPointer(this),
                            remaining.convert(),
                            0,
                            addressPtr.reinterpret(),
                            len
                        )
                    }
                } ?: 0

                if (rr == SOCKET_ERROR) {
                    if (GetLastError().convert<UInt>() == platform.windows.WSAEWOULDBLOCK.convert<UInt>()) {
                        return 0
                    }
                    if (GetLastError().convert<UInt>() == platform.windows.WSAECONNRESET.convert<UInt>()) { // 10054
                        throw IOException("Connection reset by peer.")
                    }
                    throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
                }
                netaddress.size = len[0]
                if (address !== netaddress) {
                    address.update(
                        host = netaddress.host,
                        port = netaddress.port,
                    )
                }
                rr
            }
        }
        data.position += gotBytes.toInt()
        return gotBytes
    }

    override fun processAfterSendUdp(data: ByteBuffer, code: Int): Int {
        if (code == SOCKET_ERROR) {
            if (GetLastError().toInt() == platform.windows.WSAEFAULT) { // 10014
                throw IOException("The system detected an invalid pointer address in attempting to use a pointer argument in a call.")
            }
            if (GetLastError().toInt() == platform.windows.WSAEWOULDBLOCK) { // 10035
                return 0
            }
            throw IOException("Can't send data. Error: $errno  ${GetLastError()}")
        }

        data.position += code.toInt()
        return code
    }

    override fun connect(path: String): ConnectStatus {
        throw RuntimeException("Not supported")
    }

    override fun accept(address: ((String) -> Unit)?): TcpClientNetSocket? {
        throw RuntimeException("Not supported")
    }

    override fun bind(path: String): BindStatus {
        throw RuntimeException("Not supported")
    }

    override fun send(data: ByteBuffer, address: String): Int {
        throw RuntimeException("Not supported")
    }

    override fun receive(data: ByteBuffer, address: (String) -> Unit?): Int {
        throw RuntimeException("Not supported")
    }
}
