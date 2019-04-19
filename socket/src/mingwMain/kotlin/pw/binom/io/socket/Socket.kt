package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.FIONBIO
import platform.posix.SOCK_STREAM
import platform.posix.SOMAXCONN
import platform.posix.bind
import platform.posix.closesocket
import platform.posix.connect
import platform.posix.listen
import platform.posix.recv
import platform.posix.shutdown
import platform.posix.socket
import platform.windows.*
import platform.windows.ioctlsocket
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

actual class Socket(val native: SOCKET) : Closeable, InputStream, OutputStream {

    var blocking: Boolean = true
        set(value) {
            memScoped {
                val nonBlocking = alloc<UIntVar>()
                nonBlocking.value = if (value) 0u else 1u
                if (ioctlsocket(native, FIONBIO.convert(), nonBlocking.ptr) == -1)
                    throw IOException("ioctlsocket() error")
            }
            field = value
        }

    private var _connected = false
    actual val connected: Boolean
        get() = _connected

    actual constructor() : this(socket(AF_INET, SOCK_STREAM, 0))

    override fun flush() {
        //NOP
    }

    override fun close() {
        shutdown(native, SD_SEND)
        closesocket(native)
    }

    fun bind(port: Int) {
        memScoped {
            val serverAddr = alloc<sockaddr_in>()
            with(serverAddr) {
                memset(this.ptr, 0, sockaddr_in.size.convert())
                sin_family = AF_INET.convert()
                sin_addr.S_un.S_addr = posix_htons(0).convert()//TODO что тут в линуксе
                sin_port = posix_htons(port.toShort()).convert()
            }
            if (bind(native, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) != 0)
                throw IOException("bind() error")
            if (listen(native, SOMAXCONN) != 0)
                throw IOException("listen() error")
        }
        _connected = true
    }

    actual fun connect(host: String, port: Int) {
        memScoped {
            val hints = alloc<addrinfo>()
            memset(hints.ptr, 0, sizeOf<addrinfo>().convert())

            hints.ai_flags = AI_CANONNAME
            hints.ai_family = platform.windows.AF_UNSPEC
            hints.ai_socktype = SOCK_STREAM
            hints.ai_protocol = platform.windows.IPPROTO_TCP

            val result = allocPointerTo<addrinfo>()
//                val result = Array<addrinfo>(1)
//                val result = allocArray<addrinfo>(1)
            if (getaddrinfo(host, port.toString(), hints.ptr, result.ptr) != 0) {
                throw IOException("Can't resolve host $host")
            }
            val result1 = connect(native, result.value!!.pointed.ai_addr!!.pointed.ptr, result.value!!.pointed.ai_addrlen.convert())

            freeaddrinfo(result.value)

            if (result1 == SOCKET_ERROR) {
                println("errno=$errno")
                throw SocketConnectException()
            }
            _connected = true
        }
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val r = recv(native, data.refTo(offset), length, 0)
        if (r == 0) {
            _connected = false
            throw SocketClosedException()
        }
        return r
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {

        val r = send(native, data.refTo(offset), length, 0)
        if (r <= 0) {
            _connected = false
            throw SocketClosedException()
        }
        return r
    }


    actual companion object {
        actual fun startup() {
            init_sockets()
        }

        actual fun shutdown() {
            deinit_sockets()
        }
    }
}