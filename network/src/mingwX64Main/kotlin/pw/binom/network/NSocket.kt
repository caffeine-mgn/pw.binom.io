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

actual class NSocket(val native: SOCKET) : Closeable {
    actual companion object {
        actual fun tcp(): NSocket {
            val native = socket(AF_INET, SOCK_STREAM, 0)
            if (native < 0uL) {
                throw RuntimeException("Tcp Socket Creation")
            }
            return NSocket(native)
        }

        actual fun udp(): NSocket {
            val native = socket(AF_INET, platform.windows.SOCK_DGRAM.convert(), 0)
            if (native < 0uL) {
                throw RuntimeException("Datagram Socket Creation")
            }
            return NSocket(native)
        }
    }

    actual fun accept(address: NetworkAddress.Mutable?): NSocket {
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
        if (native == INVALID_SOCKET)
            throw IOException("Can't accept new client")
        return NSocket(native)
    }

    actual fun send(data: ByteBuffer): Int {
        memScoped {
            val r: Int = send(native, data.refTo(data.position), data.remaining.convert(), 0).convert()
            if (r < 0) {
                val error = GetLastError()
                if (error == 10035.convert<DWORD>())
                    return 0
                if (error == 10038.convert<DWORD>() || error == 10054.convert<DWORD>())
                    throw SocketClosedException()
                throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
            }
            data.position += r
            return r
        }
    }

    actual fun recv(data: ByteBuffer): Int {
        val r: Int = platform.windows.recv(native, data.refTo(data.position), data.capacity.convert(), 0).convert()
        if (r < 0) {
            val error = GetLastError()
            if (error == 10035.convert<DWORD>())
                return 0
            throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
        }
        data.position += r
        return r
    }

    override fun close() {
        shutdown(native, SD_SEND)
        closesocket(native)
    }

    actual fun setBlocking(value: Boolean) {
        memScoped {
            val nonBlocking = alloc<UIntVar>()
            nonBlocking.value = if (value) 0u else 1u
            if (ioctlsocket(native, FIONBIO.convert(), nonBlocking.ptr) == -1)
                throw IOException("ioctlsocket() error")
        }
    }

    actual fun connect(address: NetworkAddress) {
        memScoped {
            val r = platform.windows.connect(
                native,
                address.data.refTo(0).getPointer(this).reinterpret(),
                address.size.convert()
            )
        }
    }

    actual fun bind(address: NetworkAddress) {
        memScoped {
            platform.windows.bind(
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
            if (rr == SOCKET_ERROR) {
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
            val rr = platform.windows.recvfrom(
                native,
                data.bytes.refTo(data.position),
                data.remaining.convert(),
                0,
                null,
                null
            )
            if (rr == SOCKET_ERROR) {
                throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
            }
            rr
        } else {
            memScoped {
                SetLastError(0)
                val len = allocArray<IntVar>(1)
                len[0] = 28
                val rr = platform.windows.recvfrom(
                    native, data.bytes.refTo(data.position).getPointer(this), data.remaining.convert(), 0,
                    address.data.refTo(0).getPointer(this).reinterpret<sockaddr>(),
                    len
                )

                if (rr == SOCKET_ERROR) {
                    throw IOException("Can't read data. Error: $errno  ${GetLastError()}")
                }
                address.size = len[0]
                rr
            }
        }
        data.position += gotBytes.toInt()
        return gotBytes
    }

}