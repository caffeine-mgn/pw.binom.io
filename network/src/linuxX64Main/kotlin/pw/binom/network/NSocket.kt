package pw.binom.network

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual typealias RawSocket = Int

private fun setBlocking(native: RawSocket, value: Boolean) {
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

private fun bind(native: RawSocket, address: NetworkAddress, family: Int) {
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
            if (address.type != NetworkAddress.Type.IPV4) {
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
            println("bind on $address. errno: $errno  ${posix_errno()}")
            if (errno == EADDRINUSE || errno == 0) {
                throw BindException("Address already in use: ${address.host}:${address.port}")
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
            native, IPPROTO_IPV6,
            IPV6_V6ONLY, flag, sizeOf<IntVar>().convert()
        )
        if (iResult == -1) {
            close(native)
            throw IOException("Can't allow ipv6 connection for UDP socket. Error: $errno")
        }
        println("set ipv6 only result: $iResult for $native")
    }
}

actual class NSocket(var native: Int, val family: Int) : Closeable {
    actual companion object {
        actual fun serverTcp(address: NetworkAddress): NSocket {
            val domain = when (address.type) {
                NetworkAddress.Type.IPV4 -> AF_INET
                NetworkAddress.Type.IPV6 -> AF_INET6
            }
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            applyReuse(native)
            bind(native, address, family = domain)
            if (address.type == NetworkAddress.Type.IPV6) {
                allowIpv4(native)
            }
            return NSocket(native, family = domain)
        }

        actual fun connectTcp(address: NetworkAddress, blocking: Boolean): NSocket {
            val domain = when (address.type) {
                NetworkAddress.Type.IPV4 -> AF_INET
                NetworkAddress.Type.IPV6 -> AF_INET6
            }
            val native = socket(domain, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            setBlocking(native, blocking)

            memScoped {
                val r = address.data.usePinned { data ->
                    set_posix_errno(0)
                    connect(
                        native,
                        data.addressOf(0).getPointer(this).reinterpret(),
                        data.get().size.convert()
                    )
                }
                if (r < 0) {
                    if (errno == EAFNOSUPPORT) {
                        throw IOException("Can't connect. Error: $errno, Address family not supported by protocol")
                    }
                    if (errno != EINPROGRESS) {
                        throw IOException("Can't connect. Error: $errno")
                    }
                }
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
    private val closed = AtomicBoolean(false)

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
                    println("getsockname=$r, errno=$errno")
                    null
                }
            }
        }

    private fun checkClosed() {
        if (closed.value) {
            throw RuntimeException("Socket already closed")
        }
    }

    actual fun accept(address: NetworkAddress.Mutable?): NSocket? {
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
        checkClosed()

        memScoped {

            val r: Int = data.ref { dataPtr, remaining ->
                send(native, dataPtr, remaining.convert(), MSG_NOSIGNAL).convert()
            }
            if (r < 0) {
                val error = errno.toInt()
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                if (error == ECONNRESET) {
                    nativeClose()
                    throw SocketClosedException()
                }
                if (errno == EBADF) {
                    throw SocketClosedException()
                }
                throw IOException("Error on send data to network. send: [$r], error: [$errno]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        checkClosed()
        val r: Int = data.ref { dataPtr, remaining ->
            recv(native, dataPtr, remaining.convert(), 0).convert()
        }
        if (r < 0) {
            if (errno == EAGAIN) {
                return 0
            }
            if (errno == ECONNRESET) {
                nativeClose()
                throw SocketClosedException()
            }
            if (errno == EBADF) {
                throw SocketClosedException()
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
        checkClosed()
        try {
            nativeClose()
        } finally {
            closed.value = true
        }
    }

    actual fun setBlocking(value: Boolean) {
        checkClosed()
        setBlocking(native, value)
    }

    actual fun connect(address: NetworkAddress) {
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

    actual fun bind(address: NetworkAddress) {
        checkClosed()
        bind(native, address, family)
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        memScoped {
            checkClosed()
            val rr = data.ref { dataPtr, remaining ->
                set_posix_errno(0)
                address.data.usePinned {
                    sendto(
                        native, dataPtr, remaining.convert(),
                        0,
                        it.addressOf(0).reinterpret(), address.size.convert()
                    )
                }
            }

            if (rr.toInt() == -1) {
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                if (errno == EINVAL) {
                    throw IOException("Can't send data: Invalid argument")
                }
                throw IOException("Can't send data. Error: $errno  $errno")
            }

            data.position += rr.toInt()
            rr
        }.convert()

    actual fun recv(
        data: ByteBuffer,
        address: NetworkAddress.Mutable?
    ): Int {
        checkClosed()
        val gotBytes = if (address == null) {

            val rr = data.ref { dataPtr, remaining ->
                recvfrom(native, dataPtr, remaining.convert(), 0, null, null)
            }
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
                        native, dataPtr.getPointer(this), remaining.convert(), 0,
                        address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(),
                        len
                    )
                }

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
