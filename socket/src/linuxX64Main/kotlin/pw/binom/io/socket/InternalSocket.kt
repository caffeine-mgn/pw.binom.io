package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.*
import platform.posix.free
import platform.posix.malloc
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.BindException
import pw.binom.io.Closeable
import pw.binom.io.IOException

internal actual fun setBlocking(native: NativeSocketHolder, value: Boolean) {
    val flags = fcntl(native.native, F_GETFL, 0)
    val newFlags = if (value)
        flags xor O_NONBLOCK
    else
        flags or O_NONBLOCK

    if (0 != fcntl(native.native, F_SETFL, newFlags))
        throw IOException()
}

internal actual fun closeSocket(native: NativeSocketHolder) {
    close(native.native)
}

actual class NativeSocketHolder(val native: Int) {
    actual val code: Int
        get() = native

}

internal actual fun initNativeSocket(): NativeSocketHolder =
        NativeSocketHolder(socket(AF_INET, SOCK_STREAM, 0))

internal actual fun recvSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int =
        recv(socket.native, data.refTo(offset), length.convert(), 0).convert()

internal actual fun recvSocket(socket: NativeSocketHolder, data: ByteDataBuffer, offset: Int, length: Int): Int =
        recv(socket.native, data.refTo(offset), length.convert(), 0).convert()

internal actual fun recvSocket(socket: NativeSocketHolder, dest: ByteBuffer): Int {
    val r = recv(socket.native, dest.native + dest.position, dest.remaining.convert(), 0).convert<Int>()
    dest.position += r
    return r
}

internal actual fun bindSocket(socket: NativeSocketHolder, host: String, port: Int) {
    memScoped {
        val serverAddr = alloc<sockaddr_in>()
        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            //sin_addr.s_addr = posix_htons(0).convert()//TODO что тут в линуксе
            sin_addr.s_addr = if (host == "0.0.0.0")
                posix_htons(0).convert<UInt>()
            else
                inet_addr(host)
            sin_port = posix_htons(port.convert()).convert()
        }
        if (bind(socket.native, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) != 0)
            throw BindException()
        if (listen(socket.native, SOMAXCONN) == -1)
            throw BindException()
    }
}

internal actual fun connectSocket(native: NativeSocketHolder, host: String, port: Int) {
    memScoped {
        val addr = alloc<sockaddr_in>()
        memset(addr.ptr, 0, sizeOf<sockaddr_in>().convert())
        addr.sin_family = AF_INET.convert()
        addr.sin_port = htons(port.toUShort())

//        if (inet_pton(AF_INET, host, addr.sin_addr.ptr) <= 0) {
//
//        }

        val server = gethostbyname(host) ?: throw SocketConnectException("Unknown host \"$host\"")
        addr.sin_addr.s_addr = server.pointed.h_addr_list!![0]!!.reinterpret<UIntVar>().pointed.value

        val r = connect(native.native, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        if (r < 0) {
            throw SocketConnectException("Can't connect to $host:$port ($r)")
        }
    }
}

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int =
        send(socket.native, data.refTo(offset), length.convert(), MSG_NOSIGNAL).convert<Int>().convert()

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteDataBuffer, offset: Int, length: Int): Int =
        send(socket.native, data.refTo(offset), length.convert(), MSG_NOSIGNAL).convert()

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteBuffer): Int {
    val r = send(socket.native, data.native + data.position, data.remaining.convert(), MSG_NOSIGNAL).convert<Int>()
    data.position += r
    return r
}

internal actual fun acceptSocket(socket: NativeSocketHolder): NativeSocketHolder {
    val native = accept(socket.native, null, null)
    if (native == -1)
        throw IOException("Can't accept new client")
    return NativeSocketHolder(native)
}

internal actual val NativeEvent.key: SocketSelector.SelectorKeyImpl
    get() = data.ptr!!.asStableRef<SocketSelector.SelectorKeyImpl>().get()

internal actual class NativeEpoll actual constructor(connectionCount: Int) {
    val native = epoll_create(connectionCount)
    actual fun free() {
        close(native)
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
                event.data.fd = socket.native
                event.data.ptr = ref.key
                epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
                ref
            }

    actual fun edit(socket: NativeSocketHolder, ref: SelfRefKey, readFlag: Boolean, writeFlag: Boolean) {
        memScoped {
            val event = alloc<epoll_event>()
            var e = EPOLLRDHUP.convert<UInt>()
            if (readFlag)
                e = e or EPOLLIN.convert()
            if (writeFlag)
                e = e or EPOLLOUT.convert()
            event.events = e.convert()
            event.data.fd = socket.native
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
    val native = nativeHeap.allocArray<epoll_event>(connectionCount)//malloc((sizeOf<epoll_event>() * connectionCount).convert())!!.reinterpret<epoll_event>()
    actual fun free() {
        nativeHeap.free(native)
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
    get() = data.fd