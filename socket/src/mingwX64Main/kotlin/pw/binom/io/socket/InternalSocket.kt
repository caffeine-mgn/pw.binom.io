package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.SOCKET
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
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.*
import pw.binom.thread.Worker
import pw.binom.thread.sleep

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

actual class NativeSocketHolder(val native: SOCKET) {
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

internal actual fun recvSocket(socket: NativeSocketHolder, data: ByteDataBuffer, offset: Int, length: Int): Int {
    val r: Int = recv(socket.native, data.refTo(offset), length.convert(), 0).convert()
    if (r < 0) {
        val error = GetLastError()
        if (error == 10035.convert<DWORD>())
            return 0
        throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
    }
    return r
}

internal actual fun recvSocket(socket: NativeSocketHolder, dest: ByteBuffer): Int{
    val r: Int = recv(socket.native, dest.native+dest.position, (dest.limit-dest.position).convert(), 0).convert()
    if (r < 0) {
        val error = GetLastError()
        if (error == 10035.convert<DWORD>())
            return 0
        throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
    }
    dest.position += r
    return r
}

internal actual fun bindSocket(socket: NativeSocketHolder, host: String, port: Int) {
    memScoped {
        val serverAddr = alloc<sockaddr_in>()
        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_addr.S_un.S_addr = if (host == "0.0.0.0")
                htons(0.convert()).convert<UInt>()
            else
                platform.posix.inet_addr(host)
            sin_port = htons(port.convert()).convert()
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
                        Worker.sleep(50)
                        continue@LOOP
                    }
                    platform.windows.WSAEISCONN -> {
                        break@LOOP
                    }
                    platform.windows.WSAECONNREFUSED -> {
                        throw ConnectException("Connection refused: connect")
                    }
                }
                throw ConnectException(host = host, port = port)
            }
            break
        }
    }
}

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int =
        send(socket.native, data.refTo(offset), length, 0)

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteBuffer): Int {
    val r: Int = send(socket.native, data.native+data.position, data.remaining.convert(), 0).convert()
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

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteDataBuffer, offset: Int, length: Int): Int {
    val r: Int = send(socket.native, data.refTo(offset), length.convert(), 0).convert()
    if (r < 0) {
        val error = GetLastError()
        if (error == 10035.convert<DWORD>())
            return 0
        if (error == 10038.convert<DWORD>() || error == 10054.convert<DWORD>())
            throw SocketClosedException()
        throw IOException("Error on send data to network. send: [$r], error: [${GetLastError()}]")
    }
    return r
}

internal actual fun acceptSocket(socket: NativeSocketHolder): NativeSocketHolder {
    val native = accept(socket.native, null, null)
    if (native == INVALID_SOCKET)
        throw IOException("Can't accept new client")
    return NativeSocketHolder(native)
}

internal actual val NativeEvent.key: SocketSelector.SelectorKeyImpl
    get() = data.ptr!!.asStableRef<SocketSelector.SelectorKeyImpl>().get()

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

    actual fun add(socket: NativeSocketHolder, key: SocketSelector.SelectorKeyImpl): SelfRefKey =
            memScoped {
                val event = alloc<epoll_event>()
                val ref = SelfRefKey(key)
                event.events = if (key.channel is ServerSocketChannel)
                    (EPOLLIN or EPOLLOUT or EPOLLRDHUP).convert()
                else
                    EPOLLRDHUP.convert()
                event.data.sock = socket.native
                event.data.ptr = ref.key
                epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
                ref
            }

    actual fun edit(socket: NativeSocketHolder, ref: SelfRefKey, readFlag: Boolean, writeFlag: Boolean) {
        memScoped {
            val event = alloc<epoll_event>()
            var e = EPOLLRDHUP
            if (readFlag)
                e = e or EPOLLIN
            if (writeFlag)
                e = e or EPOLLOUT
            event.events = e.convert()
            event.data.sock = socket.native
            event.data.ptr = ref.key
            epoll_ctl(native, EPOLL_CTL_MOD, socket.native, event.ptr)
        }
    }
}

actual typealias NativeEvent = epoll_event

internal actual class SelfRefKey(key: SocketSelector.SelectorKeyImpl) : Closeable {
    val key = StableRef.create(key).asCPointer()
    override fun close() {
        key.asStableRef<SocketSelector.SelectorKeyImpl>().dispose()
    }
}

internal actual class NativeEpollList actual constructor(connectionCount: Int) {
    val native = malloc((sizeOf<epoll_event>() * connectionCount).convert())!!.reinterpret<epoll_event>()
    actual fun free() {
        free(native)
    }

    actual inline operator fun get(index: Int): NativeEvent = native[index]
}

internal actual val NativeEvent.isClosed: Boolean
    get() = events.convert<Int>() and EPOLLRDHUP.convert() != 0

internal actual val NativeEvent.isReadable: Boolean
    get() = events.convert<Int>() and EPOLLIN.convert() != 0

internal actual val NativeEvent.isWritable: Boolean
    get() = events.convert<Int>() and EPOLLOUT.convert() != 0

internal actual val NativeEvent.socId: Int
    get() = data.sock.toInt()