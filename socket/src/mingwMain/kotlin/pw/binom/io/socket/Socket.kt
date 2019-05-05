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
import platform.windows.*
import platform.windows.ioctlsocket
import pw.binom.Thread
import pw.binom.io.*
import kotlin.native.concurrent.ensureNeverFrozen

actual class Socket(val native: SOCKET) : Closeable, InputStream, OutputStream {

    init {
        this.ensureNeverFrozen()
    }

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
    private var _closed = false

    actual val connected: Boolean
        get() = _connected

    internal fun internalDisconnected() {
        _connected = false
    }

    internal fun internalConnected() {
        _connected = true
    }

    actual constructor() : this(initNativeSocket())

    override fun flush() {
        //NOP
    }

    override fun close() {
        shutdown(native, SD_SEND)
        closesocket(native)
        _closed = true
        internalDisconnected()
    }

    private var bindded = false

    fun bind(port: Int) {
        if (bindded)
            throw SocketException("Port was bind")
        portCheck(port)

        memScoped {
            val serverAddr = alloc<sockaddr_in>()
            with(serverAddr) {
                memset(this.ptr, 0, sockaddr_in.size.convert())
                sin_family = AF_INET.convert()
                sin_addr.S_un.S_addr = posix_htons(0).convert()//TODO что тут в линуксе
                sin_port = posix_htons(port.toShort()).convert()
            }
            if (bind(native, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) != 0) {
                throw when (platform.windows.WSAGetLastError()) {
                    platform.windows.WSAEADDRINUSE -> BindException()
                    else -> IOException("bind() error. errno=${platform.windows.WSAGetLastError()}")
                }
            }
            if (listen(native, SOMAXCONN) != 0)
                throw IOException("listen() error")
        }
        _connected = true
        bindded = true
    }

    actual fun connect(host: String, port: Int) {

        portCheck(port)

        memScoped {
            val hints = alloc<addrinfo>()
            memset(hints.ptr, 0, sizeOf<addrinfo>().convert())

            hints.ai_flags = AI_CANONNAME
            hints.ai_family = platform.windows.AF_UNSPEC
            hints.ai_socktype = SOCK_STREAM
            hints.ai_protocol = platform.windows.IPPROTO_TCP

            LOOP@ while (true) {
                println("Turn connect...")
                val result = allocPointerTo<addrinfo>()
                if (getaddrinfo(host, port.toString(), hints.ptr, result.ptr) != 0) {
                    throw UnknownHostException(host)
                }
                val result1 = connect(
                        native,
                        result.value!!.pointed.ai_addr!!.pointed.ptr,
                        result.value!!.pointed.ai_addrlen.convert()
                )

                freeaddrinfo(result.value)

                if (result1 == SOCKET_ERROR) {
                    when (platform.windows.WSAGetLastError()) {
                        platform.windows.WSAEWOULDBLOCK -> {
                            Thread.sleep(50)
                            continue@LOOP
                        }
                        platform.windows.WSAEISCONN -> {
                            internalConnected()
                            break@LOOP
                        }
                        platform.windows.WSAECONNREFUSED -> {
                            throw ConnectException("Connection refused: connect")
                        }
                    }
                    println("Connect ERROR: ${platform.windows.WSAGetLastError()}")
                    throw ConnectException(host = host, port = port)
                }
                internalConnected()
                break
            }
        }
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val r = recv(native, data.refTo(offset), length, 0)
        if (r <= 0) {
            close()
            throw SocketClosedException()
        }
        return r
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (closed)
            throw SocketClosedException()
        if (!connected)
            throw IOException("Socket is not connected")

        if (length == 0)
            return 0
        if (offset + length > data.size)
            throw ArrayIndexOutOfBoundsException()
        val r = send(native, data.refTo(offset), length, 0)
        if (r <= 0) {
            _connected = false
            throw SocketClosedException()
        }
        return r
    }

    actual val closed: Boolean
        get() = _closed
}

internal fun portCheck(port: Int) {
    if (port < 0 || port > 0xFFFF)
        throw IllegalArgumentException("port out of range:$port")
}