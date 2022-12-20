package pw.binom.io.socket

import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.posix.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException

class PosixSocket(
    native: RawSocket,
    server: Boolean
) : AbstractSocket(native = native, server = server) {

    override fun bind(address: NetworkAddress): BindStatus {
        memScoped {
            val netAddress = if (address is CommonMutableNetworkAddress) {
                address
            } else {
                CommonMutableNetworkAddress(address)
            }
            val bindResult = netAddress.getAsIpV6 { ipv6Addr ->
                bind(
                    native, ipv6Addr.reinterpret(), sizeOf<sockaddr_in6>().convert()
                )
            }

            if (bindResult < 0) {
                val error = errno
                when (error) {
                    EINVAL -> return BindStatus.ALREADY_BINDED
                    EADDRINUSE -> return BindStatus.ADDRESS_ALREADY_IN_USE
                }
//                if (errno == EADDRINUSE || errno == 0) {
//                    throw BindException("Address already in use: ${address.host}:${address.port}")
//                }
//                if (errno == EACCES) {
//                    throw BindException("Can't bind to $address: Permission denied")
//                }
                throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
            }
            val listenResult = listen(native, 1000)
            if (listenResult < 0) {
                val errno = errno
                when (errno) {
                    EOPNOTSUPP -> {
                        // Do nothing
                    }

                    else -> {
                        unbind(native)
                        throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
                    }
                }
//                when (errno) {
//                    EINVAL -> throw BindException("Socket already binded")
//                }
//                if (errno == ESOCKTNOSUPPORT) {
//                    return@memScoped
//                }
//                if (errno == EOPNOTSUPP) {
//                    return@memScoped
// //                   unbind(native)
// //                   throw IOException("Can't bind socket: Operation not supported on transport endpoint")
//                }
            }
        }
        return BindStatus.OK
    }

    override fun connect(address: NetworkAddress): ConnectStatus {
        val netAddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        val connectResponse = netAddress.getAsIpV6 { addr ->
            connect(
                native,
                addr.reinterpret(),
                sizeOf<sockaddr_in6>().convert(),
            )
        }
        if (connectResponse < 0) {
            val errno = errno
            return when (errno) {
                ECONNREFUSED -> ConnectStatus.CONNECTION_REFUSED
                EISCONN -> ConnectStatus.ALREADY_CONNECTED
                EINPROGRESS -> ConnectStatus.IN_PROGRESS
                else -> throw IOException("Invalid response $connectResponse. errno: $errno")
            }
        }
        return ConnectStatus.OK
    }

    override fun send(data: ByteBuffer, address: NetworkAddress): Int {
        if (data.remaining == 0) {
            return 0
        }
        val netAddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        val sendResult = netAddress.getAsIpV6 { ipv6Addr ->
            data.ref { ptr, remaining ->
                sendto(
                    native,
                    ptr,
                    remaining.convert(),
                    0,
                    ipv6Addr.reinterpret(),
                    sizeOf<sockaddr_in6>().convert(),
                )
            }
        }!!.toInt()
        if (sendResult == -1) {
            if (errno == EPIPE) {
                return -1
            }
            if (errno == EINVAL) {
                throw IOException("Can't send data: Invalid argument")
            }
            if (errno == EAGAIN) {
                return 0
            }
            throw IOException("Can't send data. Error: $errno  $errno")
        }
        data.position += sendResult
        return sendResult
    }

    override fun processAfterSendUdp(data: ByteBuffer, code: Int): Int {
        if (code == -1) {
            if (errno == EPIPE) {
                return -1
            }
            if (errno == EINVAL) {
                throw IOException("Can't send data: Invalid argument")
            }
            if (errno == EAGAIN) {
                return 0
            }
            throw IOException("Can't send data. Error: $errno  $errno")
        }
        data.position += code
        return code
    }

    override fun connect(path: String): ConnectStatus {
        TODO("Not yet implemented")
    }

    override fun send(data: ByteBuffer, address: String): Int {
        TODO("Not yet implemented")
    }

    override fun receive(data: ByteBuffer, address: (String) -> Unit?): Int {
        TODO("Not yet implemented")
    }

    override fun bind(path: String): BindStatus = bindUnixSocket(native = native, fileName = path)

    override fun send(data: ByteBuffer): Int {
        if (closed) {
            return -1
        }
        if (!data.isReferenceAccessAvailable()) {
            return 0
        }
        memScoped {
            val r: Int = data.ref { dataPtr, remaining ->
                send(native, dataPtr, remaining.convert(), MSG_NOSIGNAL).convert()
            } ?: 0
            if (r < 0) {
                val error = errno.toInt()
                if (errno == EPIPE) {
                    nativeClose()
                    return -1
                }
                if (error == ECONNRESET) {
                    nativeClose()
                    return -1
                }
                if (errno == EAGAIN) {
                    return 0
                }
                if (errno == EBADF) {
                    return -1
                }
                throw IOException("Error on send data to network. send: [$r], error: [$errno]")
            }
            data.position += r
            return r
        }
    }

    override fun receive(data: ByteBuffer): Int {
        if (closed) {
            return -1
        }
        if (!data.isReferenceAccessAvailable()) {
            return 0
        }
        val r = data.ref { dataPtr, remaining ->
            recv(native, dataPtr, remaining.convert(), 0).convert()
        } ?: 0
        if (r == 0) {
            return -1
        }
        if (r < 0) {
            if (errno == EAGAIN) {
                return 0
            }
            if (errno == ECONNRESET) {
                nativeClose()
                return -1
//                throw SocketClosedException()
            }
            if (errno == EBADF) {
                return -1
//                throw SocketClosedException()
            }
            nativeClose()
//            TODO("Отслеживать отключение сокета. send: [$r], error: [${errno}], EDEADLK=$EDEADLK")
            throw IOException("Error on read data to network. send: [$r], error: [$errno]")
        }
        if (r > 0) {
            data.position += r
        }
        return r
    }

    override fun accept(address: MutableNetworkAddress?): TcpClientNetSocket? {
        val clientRaw = internalAccept(native, address) ?: return null
        return PosixSocket(native = clientRaw, server = false)
    }

    override fun accept(address: ((String) -> Unit)?): TcpClientNetSocket? {
        TODO("Not yet implemented")
    }
}
