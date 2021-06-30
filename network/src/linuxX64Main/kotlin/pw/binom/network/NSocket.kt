package pw.binom.network

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class NSocket(val native: Int) : Closeable {
    actual companion object {
        actual fun tcp(): NSocket {
            val native = socket(AF_INET, SOCK_STREAM, 0)
            if (native < 0) {
                throw RuntimeException("Tcp Socket Creation")
            }
            return NSocket(native)
        }

        actual fun udp(): NSocket {
            val native = socket(AF_INET, SOCK_DGRAM.convert(), 0)
            if (native < 0) {
                throw RuntimeException("Datagram Socket Creation")
            }
            return NSocket(native)
        }
    }

    private val closed = AtomicBoolean(false)

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
        return NSocket(native)
    }

    actual fun send(data: ByteBuffer): Int {
        checkClosed()
        memScoped {
            val r: Int = send(native, data.refTo(data.position), data.remaining.convert(), MSG_NOSIGNAL).convert()
            if (r < 0) {
                val error = errno.toInt()
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                if (error == ECONNRESET) {
                    nativeClose()
                    throw SocketClosedException()
                }
                if (errno == EBADF){
                    nativeClose()
                    throw SocketClosedException()
                }
                throw IOException("Error on send data to network. send: [$r], error: [${errno}]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        checkClosed()
        val r: Int = recv(native, data.refTo(data.position), data.remaining.convert(), 0).convert()
        if (r < 0) {
            if (errno == EAGAIN) {
                return 0
            }
            if (errno == 104) {
                nativeClose()
                throw SocketClosedException()
            }
            if (errno == EBADF){
                nativeClose()
                throw SocketClosedException()
            }
            nativeClose()
//            TODO("Отслеживать отключение сокета. send: [$r], error: [${errno}], EDEADLK=$EDEADLK")
            throw IOException("Error on read data to network. send: [$r], error: [${errno}]")
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
        }
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

    actual fun connect(address: NetworkAddress) {
        checkClosed()
        memScoped {
            set_posix_errno(0)
            val r = connect(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (r < 0 && errno != EINPROGRESS) {
                throw IOException("Can't connect")
            }
        }
    }

    actual fun bind(address: NetworkAddress) {
        checkClosed()
        memScoped {
            set_posix_errno(0)
            val bindResult = bind(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            if (bindResult < 0) {
                if (errno == 98) {
                    throw BindException("Address already in use: ${address.host}:${address.port}")
                }
                throw IOException("Bind error. errno: [$errno], bind: [$bindResult]")
            }
            val listenResult = listen(native, 1000)
            if (listenResult < 0) {
                if (errno == 95) {
                    return@memScoped
                }
                throw IOException("Listen error. errno: [$errno], listen: [$listenResult]")
            }
        }
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        memScoped {
            checkClosed()
            val rr = sendto(
                native, data.refTo(data.position).getPointer(this), data.remaining.convert(),
                MSG_NOSIGNAL,
                address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(), address.size.convert()
            )
            if (rr.toInt() == -1) {
                if (errno == EPIPE) {
                    throw SocketClosedException()
                }
                throw IOException("Can't send data. Error: $errno  ${errno}")
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
            val rr = recvfrom(native, data.refTo(data.position), data.remaining.convert(), 0, null, null)
            if (rr.toInt() == -1) {
                throw IOException("Can't read data. Error: $errno  ${errno}")
            }
            rr
        } else {
            memScoped {
                val len = allocArray<socklen_tVar>(1)
                len[0] = 28.convert()
                val rr = recvfrom(
                    native, data.refTo(data.position).getPointer(this), data.remaining.convert(), 0,
                    address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(),
                    len
                )

                if (rr.toInt() == -1) {
                    throw IOException("Can't read data. Error: $errno  ${errno}")
                }
                address.size = len[0].convert()
                rr
            }
        }.convert<Int>()
        data.position += gotBytes.toInt()
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