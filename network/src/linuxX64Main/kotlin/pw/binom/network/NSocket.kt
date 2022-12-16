package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.sockaddr_un
import platform.posix.*
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual typealias RawSocket = Int

internal fun setBlocking(native: RawSocket, value: Boolean) {
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

private fun bind(native: RawSocket, fileName: String) {
    memScoped {
        val addr = alloc<sockaddr_un>()
        memset(addr.ptr, 0, sizeOf<sockaddr_un>().convert())
        addr.sun_family = AF_UNIX.convert()
        strcpy(addr.sun_path, fileName)
        unlink(fileName)
        val bindResult = platform.posix.bind(
            native,
            addr.ptr.getPointer(this).reinterpret(),
            sizeOf<sockaddr_un>().convert()
        )

        if (bindResult < 0) {
            if (errno == EADDRINUSE || errno == 0) {
                throw BindException("Address already in use: \"$fileName\"")
            }
            throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
        }
        val listenResult = listen(native, 1000)
        if (listenResult < 0) {
            if (errno == ESOCKTNOSUPPORT) {
                return@memScoped
            }
            if (errno == EOPNOTSUPP) {
                return@memScoped
//                    unbind(native)
//                    throw IOException("Can't bind socket: Operation not supported on transport endpoint")
            }
            unbind(native)
            throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
        }
    }
}

private fun bind(native: RawSocket, address: NetworkAddressOld, family: Int) {
    memScoped {
        val bindResult = if (family == AF_INET6) {
            address.isAddrV6 { data ->
                bind(
                    native,
                    data.reinterpret(),
                    sizeOf<sockaddr_in6>().convert()
                )
            }
        } else {
            if (address.type != NetworkAddressOld.Type.IPV4) {
                throw BindException("Can't bind to $address to IPV4")
            }
            address.data.usePinned {
                bind(
                    native,
                    it.addressOf(0).getPointer(this).reinterpret(),
                    address.size.convert()
                )
            }
        }

        if (bindResult < 0) {
            if (errno == EADDRINUSE || errno == 0) {
                throw BindException("Address already in use: ${address.host}:${address.port}")
            }
            if (errno == EACCES) {
                throw BindException("Can't bind to $address: Permission denied")
            }
            throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
        }
        val listenResult = listen(native, 1000)
        if (listenResult < 0) {
            if (errno == ESOCKTNOSUPPORT) {
                return@memScoped
            }
            if (errno == EOPNOTSUPP) {
                return@memScoped
//                    unbind(native)
//                    throw IOException("Can't bind socket: Operation not supported on transport endpoint")
            }
            unbind(native)
            throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
        }
    }
}

private fun unbind(native: Int) {
    memScoped {
        val flag = alloc<IntVar>()
        flag.value = 1
        setsockopt(native, SOL_SOCKET, SO_REUSEADDR, flag.ptr, sizeOf<IntVar>().convert())
    }
}

private fun applyReuse(native: Int) {
    memScoped {
        val flag = alloc<IntVar>()
        flag.value = 1
        setsockopt(native, SOL_SOCKET, SO_REUSEADDR, flag.ptr, sizeOf<IntVar>().convert())
    }
}

private fun allowIpv4(native: RawSocket) {
    memScoped {
        val flag = allocArray<IntVar>(1)
        flag[0] = 0
        val iResult = setsockopt(
            native,
            IPPROTO_IPV6,
            IPV6_V6ONLY,
            flag,
            sizeOf<IntVar>().convert()
        )
        if (iResult == -1) {
            close(native)
            throw IOException("Can't allow ipv6 connection for UDP socket. Error: $errno")
        }
    }
}

actual class NSocket(val native: Int, val family: Int) : Closeable {
    actual companion object {
        actual fun serverTcp(address: NetworkAddressOld): NSocket {
            val domain = when (address.type) {
                NetworkAddressOld.Type.IPV4 -> AF_INET
                NetworkAddressOld.Type.IPV6 -> AF_INET6
            }
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            applyReuse(native)
            bind(native, address, family = domain)
            if (address.type == NetworkAddressOld.Type.IPV6) {
                allowIpv4(native)
            }
            return NSocket(native, family = domain)
        }

        actual fun serverTcpUnixSocket(fileName: String): NSocket {
            val domain = PF_UNIX
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            applyReuse(native)
            bind(native, fileName)
            return NSocket(native, family = domain)
        }

        private fun socketResultProcessing(r: Int) {
            if (r < 0) {
//                val errno = errno
                println("NSocket::socketResultProcessing errno=$errno posix_errno=${posix_errno()}")
                if (errno == EAFNOSUPPORT) {
                    throw IOException("Can't connect. Error: $errno, Address family not supported by protocol")
                }
                if (errno != EINPROGRESS) {
                    println("try throw exception!")
                    throw IOException("Can't connect. Error: $errno")
                }
                if (errno == EINPROGRESS) {
                    return // All is ok
                }
                println("try throw exception!")
                throw IOException("Unknown socket error $r $errno")
            }
        }

        actual fun connectTcpUnixSocket(fileName: String, blocking: Boolean): NSocket {
            val domain = PF_UNIX
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            setBlocking(native, blocking)

            memScoped {
                val addr = alloc<sockaddr_un>()
                memset(addr.ptr, 0, sizeOf<sockaddr_un>().convert())
                addr.sun_family = AF_UNIX.convert()
                strcpy(addr.sun_path, fileName)
                set_posix_errno(0)
                val r = connect(
                    native,
                    addr.ptr.reinterpret(),
                    sizeOf<sockaddr_un>().convert()
                )

                socketResultProcessing(r)
            }
            return NSocket(native, family = domain)
        }

        actual fun connectTcp(address: NetworkAddressOld, blocking: Boolean): NSocket {
            val domain = when (address.type) {
                NetworkAddressOld.Type.IPV4 -> AF_INET
                NetworkAddressOld.Type.IPV6 -> AF_INET6
            }
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            setBlocking(native, blocking)

            memScoped {
                println("NSocket: Connect to $address, blocking=$blocking")
                val r = address.data.usePinned { data ->
                    connect(
                        native,
                        data.addressOf(0).getPointer(this).reinterpret(),
                        data.get().size.convert()
                    )
                }
                println("NSocket: Finished connect to $address with $r, errno: $errno")
                socketResultProcessing(r)
            }
            return NSocket(native, family = domain)
        }

        actual fun udp(): NSocket {
            val native = socket(AF_INET6, SOCK_DGRAM.convert(), 0)
            if (native < 0) {
                throw RuntimeException("Datagram Socket Creation")
            }
            allowIpv4(native)
            return NSocket(native = native, family = AF_INET6)
        }
    }

    actual val raw: RawSocket
        get() = native.convert()
    private var closed = false

    actual val port: Int?
        get() {
            return memScoped {
                val sin = alloc<sockaddr_in>()
                val addrlen = alloc<socklen_tVar>()
                addrlen.value = sizeOf<sockaddr_in>().convert()
                val r = getsockname(native, sin.ptr.reinterpret(), addrlen.ptr)
                if (r == 0) {
                    ntohs(sin.sin_port).toInt()
                } else {
                    null
                }
            }
        }

    private fun checkClosed() {
        if (closed) {
            throw RuntimeException("Socket already closed")
        }
    }

    actual fun accept(address: NetworkAddressOld.Mutable?): NSocket? {
        checkClosed()
        val native = if (address == null) {
            accept(native, null, null)
        } else {
            memScoped {
                val len = allocArray<socklen_tVar>(1)
                len[0] = 28.convert()
                val rr = accept(
                    native,
                    address.data.refTo(0).getPointer(this).reinterpret(),
                    len
                )
                address.size = len[0].convert()
                rr
            }
        }
        if (native == -1) {
            return null
        }
        return NSocket(native = native, family = family)
    }

    actual fun send(data: ByteBuffer): Int {
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
//                    throw SocketClosedException()
                }
                if (errno == EAGAIN) {
                    return 0
                }
                if (errno == EBADF) {
                    return -1
//                    throw SocketClosedException()
                }
                throw IOException("Error on send data to network. send: [$r], error: [$errno]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        checkClosed()
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

    private fun nativeClose() {
        unbind(native)
        shutdown(native, SHUT_RDWR)
        close(native)
    }

    override fun close() {
        if (closed) {
            return
        }
        try {
            nativeClose()
        } finally {
            closed = true
        }
    }

    actual fun setBlocking(value: Boolean) {
        checkClosed()
        setBlocking(native, value)
    }

    actual fun connect(address: NetworkAddressOld) {
        checkClosed()
        memScoped {
            set_posix_errno(0)
            val r = connect(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (r < 0) {
                if (errno == EAFNOSUPPORT) {
                    throw IOException("Can't connect. Error: $errno, Address family not supported by protocol")
                }
                if (errno != EINPROGRESS) {
                    throw IOException("Can't connect. Error: $errno")
                }
            }
        }
    }

    actual fun bind(address: NetworkAddressOld) {
        checkClosed()
        bind(native, address, family)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddressOld): Int =
        run {
            checkClosed()
            if (data.remaining == 0) {
                return@run 0
            }
            val rr = data.ref { dataPtr, remaining ->
                set_posix_errno(0)
                address.data.usePinned {
                    sendto(
                        native,
                        dataPtr,
                        remaining.convert(),
                        0,
                        it.addressOf(0).reinterpret(),
                        address.size.convert()
                    )
                }
            } ?: 0

            if (rr.toInt() == -1) {
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                if (errno == EINVAL) {
                    throw IOException("Can't send data: Invalid argument")
                }
                if (errno == EAGAIN) {
                    return@run 0
                }
                throw IOException("Can't send data. Error: $errno  $errno")
            }

            data.position += rr.toInt()
            rr
        }.convert()

    actual fun recv(
        data: ByteBuffer,
        address: NetworkAddressOld.Mutable?
    ): Int {
        checkClosed()
        if (data.remaining == 0) {
            return 0
        }
        val gotBytes = if (address == null) {
            val rr = data.ref { dataPtr, remaining ->
                recvfrom(native, dataPtr, remaining.convert(), 0, null, null)
            } ?: 0
            if (rr.toInt() == -1 && errno != EAGAIN) {
                throw IOException("Can't read data. Error: $errno  $errno")
            }
            rr
        } else {
            memScoped {
                val len = allocArray<socklen_tVar>(1)
                len[0] = 28.convert()

                val rr = data.ref { dataPtr, remaining ->
                    recvfrom(
                        native,
                        dataPtr.getPointer(this),
                        remaining.convert(),
                        0,
                        address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(),
                        len
                    )
                } ?: 0

                if (rr.toInt() == -1 && errno != EAGAIN) {
                    throw IOException("Can't read data. Error: $errno  $errno")
                }
                address.size = len[0].convert()
                rr
            }
        }.convert<Int>()
        if (gotBytes > 0) {
            data.position += gotBytes.toInt()
        }
        return gotBytes
    }
}

fun isConnected(native: Int): Boolean {
    memScoped {
        val error = alloc<IntVar>()
        error.value = 0
        val len = alloc<socklen_tVar>()
        len.value = sizeOf<IntVar>().convert()
        val retval = getsockopt(native, SOL_SOCKET, SO_ERROR, error.ptr, len.ptr)
    }
    return false
}
