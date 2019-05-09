package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import platform.linux.SOCKET
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.SOMAXCONN
import platform.posix.bind
import platform.posix.connect
import platform.posix.listen
import platform.posix.recv
import platform.posix.socket
import platform.windows.*
import platform.windows.FIONBIO
import platform.windows.accept
import platform.windows.closesocket
import platform.windows.ioctlsocket
import platform.windows.shutdown
import pw.binom.Thread
import pw.binom.io.BindException
import pw.binom.io.ConnectException
import pw.binom.io.IOException
import pw.binom.io.UnknownHostException

internal actual fun setBlocking(native: NativeSocketHolder, value: Boolean) {
    memScoped {
        val nonBlocking = alloc<UIntVar>()
        nonBlocking.value = if (value) 0u else 1u
        if (ioctlsocket(native.native, FIONBIO.convert(), nonBlocking.ptr) == -1)
            throw IOException("ioctlsocket() error")
    }
}

internal actual fun closeSocket(native: NativeSocketHolder) {
    shutdown(native.native, SD_SEND)
    closesocket(native.native)
}

internal actual class NativeSocketHolder(val native: SOCKET) {
    actual val code: Int
        get() = native.toInt()
}

private var socketInited = false
internal actual fun initNativeSocket(): NativeSocketHolder {
    if (!socketInited)
        init_sockets()
    socketInited = true
    return NativeSocketHolder(socket(AF_INET, SOCK_STREAM, 0))
}

internal actual fun recvSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int =
        recv(socket.native, data.refTo(offset), length.convert(), 0).convert()

internal actual fun bindSocket(socket: NativeSocketHolder, host: String, port: Int) {
    memScoped {
        val serverAddr = alloc<sockaddr_in>()
        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_addr.S_un.S_addr = if (host == "0.0.0.0")
                posix_htons(0).convert<UInt>()
            else
                platform.posix.inet_addr(host)
            sin_port = posix_htons(port.toShort()).convert()
        }
        if (bind(socket.native, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) != 0) {
            throw when (platform.windows.WSAGetLastError()) {
                platform.windows.WSAEADDRINUSE -> BindException()
                else -> IOException("bind() error. errno=${platform.windows.WSAGetLastError()}")
            }
        }
        if (listen(socket.native, SOMAXCONN) != 0)
            throw IOException("listen() error")
    }
}

internal actual fun connectSocket(native: NativeSocketHolder, host: String, port: Int) {
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
                    native.native,
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
                        break@LOOP
                    }
                    platform.windows.WSAECONNREFUSED -> {
                        throw ConnectException("Connection refused: connect")
                    }
                }
                println("Connect ERROR: ${platform.windows.WSAGetLastError()}")
                throw ConnectException(host = host, port = port)
            }
            break
        }
    }
}

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int) {
    send(socket.native, data.refTo(offset), length, 0)
}

internal actual fun acceptSocket(socket: NativeSocketHolder): NativeSocketHolder {
    val native = accept(socket.native, null, null)
    if (native == INVALID_SOCKET)
        throw IOException("Can't accept new client")
    return NativeSocketHolder(native)
}

internal actual class NativeEpoll actual constructor(connectionCount: Int) {
    val native = epoll_create(connectionCount)!!
    actual fun free() {
        epoll_close(native)
    }

    actual fun wait(list: NativeEpollList, connectionCount: Int, timeout: Int): Int =
            epoll_wait(native, list.native, connectionCount, timeout)

    actual fun remove(socket: NativeSocketHolder) {
        epoll_ctl(native, EPOLL_CTL_DEL, socket.native, null)
    }

    actual fun add(socket: NativeSocketHolder) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = (EPOLLIN or EPOLLRDHUP).convert()
            event.data.sock = socket.native
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }
    }
}

actual typealias NativeEvent = epoll_event

internal actual class NativeEpollList actual constructor(connectionCount: Int) {
    val native = malloc((sizeOf<epoll_event>() * connectionCount).convert())!!.reinterpret<epoll_event>()
    actual fun free() {
        free(native)
    }

    actual operator fun get(index: Int): NativeEvent = native[index]
}

internal actual val NativeEvent.isClosed: Boolean
    get() = events.convert<Int>() and EPOLLRDHUP.convert() != 0
internal actual val NativeEvent.socId: Int
    get() = data.sock.toInt()