package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.internal_ntohs
import platform.posix.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class NSocket(val native: Int) : Closeable {
    actual companion object {
        actual fun tcp(): NSocket {
            val native = socket(AF_INET, SOCK_STREAM, 0)
            if (native < 0) {
                throw IOException("Can't create tcp socket")
            }
            return NSocket(native)
        }

        actual fun udp(): NSocket {
            val native = socket(AF_INET, SOCK_DGRAM.convert(), 0)
            if (native < 0) {
                throw IOException("Can't create udp socker")
            }
            return NSocket(native)
        }
    }

    init {
        memScoped {
            val flag = alloc<IntVar>()
            flag.value = 1
            setsockopt(native, SOL_SOCKET, SO_NOSIGPIPE, flag.ptr, sizeOf<IntVar>().convert())
        }
    }

    actual val port: Int?
        get() {
            return memScoped {
                val sin = alloc<sockaddr_in>()
                val addrlen = alloc<socklen_tVar>()
                addrlen.value = sizeOf<sockaddr_in>().convert()
                val r = getsockname(native, sin.ptr.reinterpret(), addrlen.ptr)
                if (r == 0) {
                    internal_ntohs(sin.sin_port).toInt()
                } else {
                    // println("getsockname=$c, errno=${errno},GetLastError()=${GetLastError()}")
                    null
                }
            }
        }

    actual fun accept(address: NetworkAddressOld.Mutable?): NSocket? {
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
        if (native == -1)
            return null // throw IOException("Can't accept new client")
        return NSocket(native)
    }

    actual fun send(data: ByteBuffer): Int {
        memScoped {
            val r: Int = data.ref { dataPtr, remaining ->
                send(native, dataPtr, remaining.convert(), MSG_NOSIGNAL).convert()
            }
//            val r: Int = send(native, data.refTo(data.position), data.remaining.convert(), 0).convert()
            if (r < 0) {
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                if (errno == EBADF) {
                    nativeClose()
                    throw SocketClosedException()
                }
                throw IOException("Error on send data to network. send: [$r], error: [$errno]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        val r: Int = data.ref { dataPtr, remaining ->
            recv(native, dataPtr, remaining.convert(), 0).convert()
        }
//        val r: Int = recv(native, data.refTo(data.position), data.remaining.convert(), 0).convert()
        if (r == 0) {
            return -1
        }
        if (r < 0) {
            if (errno == EAGAIN) {
                return 0
            }
            if (errno == EBADF) {
                return -1
//                nativeClose()
//                throw SocketClosedException()
            }
            throw IOException("Error on read data to network. send: [$r], error: [$errno]")
        }
        if (r > 0) {
            data.position += r
        }
        return r
    }

    private fun nativeClose() {
        memScoped {
            val flag = alloc<IntVar>()
            flag.value = 1
            setsockopt(native, SOL_SOCKET, SO_REUSEADDR, flag.ptr, sizeOf<IntVar>().convert())
            setsockopt(native, SOL_SOCKET, SO_REUSEPORT, flag.ptr, sizeOf<IntVar>().convert())
        }
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
            closed.value = true
        }
    }

    actual fun setBlocking(value: Boolean) {
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

    actual fun connect(address: NetworkAddressOld) {
        memScoped {
            set_posix_errno(0)
            val r = connect(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (r < 0 && errno != EINPROGRESS) {
                throw IOException("Can't connect. errno = $errno")
            }
        }
    }

    actual fun bind(address: NetworkAddressOld) {
        memScoped {
            set_posix_errno(0)
            val bindResult = bind(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (bindResult < 0) {
                if (errno == 48) {
                    throw BindException("Address already in use: ${address.host}:${address.port}")
                }
                throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
            }
            val listenResult = listen(native, 1000)
            if (listenResult < 0) {
                if (errno == 102) {
                    return@memScoped
                }
                throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
            }
        }
    }

    private val closed = AtomicBoolean(false)
    private fun checkClosed() {
        if (closed.value) {
            throw RuntimeException("Socket already closed")
        }
    }

    actual fun send(data: ByteBuffer, address: NetworkAddressOld): Int =
        memScoped {
            checkClosed()
            val rr = data.ref { dataPtr, remaining ->
                sendto(
                    native, dataPtr.getPointer(this), remaining.convert(),
                    MSG_NOSIGNAL,
                    address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(), address.size.convert()
                )
            }
//            val rr = sendto(
//                native, data.refTo(data.position).getPointer(this), data.remaining.convert(),
//                0, address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(), address.size.convert()
//            )
            if (rr.toInt() == -1) {
                throw IOException("Can't send data. Error: $errno  $errno")
            }
            if (rr > 0) {
                data.position += rr.toInt()
            }
            rr
        }.convert()

    actual fun recv(
        data: ByteBuffer,
        address: NetworkAddressOld.Mutable?
    ): Int {
        val gotBytes = if (address == null) {
            val rr = data.ref { dataPtr, remaining ->
                recvfrom(native, dataPtr, remaining.convert(), 0, null, null)
            }
//            val rr = recvfrom(native, data.refTo(data.position), data.remaining.convert(), 0, null, null)
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
//                val rr = recvfrom(
//                    native, data.refTo(data.position).getPointer(this), data.remaining.convert(), 0,
//                    address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(),
//                    len
//                )
                if (rr.toInt() == -1 && errno != EAGAIN) {
                    throw IOException("Can't read data. Error: $errno")
                }
                address.size = len[0].convert()
                rr
            }
        }.convert<Int>()

        if (gotBytes > 0) {
            data.position += gotBytes
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
