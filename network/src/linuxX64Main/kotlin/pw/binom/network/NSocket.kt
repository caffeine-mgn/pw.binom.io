package pw.binom.network

import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.socket
import pw.binom.ByteBuffer
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

    actual fun accept(address: NetworkAddress.Mutable?): NSocket {
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
            throw IOException("Can't accept new client")
        return NSocket(native)
    }

    actual fun send(data: ByteBuffer): Int {
        memScoped {
            val r: Int = send(native, data.refTo(data.position), data.remaining.convert(), 0).convert()
            if (r < 0) {
                val error = errno.toInt()
                if (error == 10035)
                    return 0
                if (error == 10038 || error == 10054)
                    throw SocketClosedException()
                throw IOException("Error on send data to network. send: [$r], error: [${errno}]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        val r: Int = recv(native, data.refTo(data.position), data.capacity.convert(), 0).convert()
        if (r < 0) {
            val error = errno
            if (error == 10035)
                return 0
            throw IOException("Error on send data to network. send: [$r], error: [${errno}]")
        }
        data.position += r
        return r
    }

    override fun close() {
        shutdown(native, SHUT_RDWR)
        close(native)
    }

    actual fun setBlocking(value: Boolean) {
        println("setBlocking($value)")
        val flags = fcntl(native, F_GETFL, 0)
        val newFlags = if (value)
            flags xor O_NONBLOCK
        else
            flags or O_NONBLOCK

        if (0 != fcntl(native, F_SETFL, newFlags))
            throw IOException()
    }

    actual fun connect(address: NetworkAddress) {
        memScoped {
            val r = connect(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
            println("Connect result: $r")
            if (r < 0 && errno != EINPROGRESS) {
                throw IOException("Can't connect")
            }
        }
    }

    actual fun bind(address: NetworkAddress) {
        memScoped {
            bind(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
        }
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int =
        memScoped {
            val rr = sendto(
                native, data.refTo(data.position).getPointer(this), data.remaining.convert(),
                0, address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(), address.size.convert()
            )
            if (rr.toInt() == -1) {
                throw IOException("Can't send data. Error: $errno  ${errno}")
            }

            data.position += rr.toInt()
            rr
        }.convert()

    actual fun recv(
        data: ByteBuffer,
        address: NetworkAddress.Mutable?
    ): Int {
        val gotBytes = if (address == null) {
            val rr = recvfrom(native, data.bytes.refTo(data.position), data.remaining.convert(), 0, null, null)
            if (rr.toInt() == -1) {
                throw IOException("Can't read data. Error: $errno  ${errno}")
            }
            rr
        } else {
            memScoped {
                val len = allocArray<socklen_tVar>(1)
                len[0] = 28.convert()
                val rr = recvfrom(
                    native, data.bytes.refTo(data.position).getPointer(this), data.remaining.convert(), 0,
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
        println("retval=${retval}   ${error.value}")
    }
    return false
}