package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.darwin.*
import platform.darwin.sockaddr_in
import platform.darwin.sockaddr_in6
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
            sin_len = sizeOf<sockaddr_in>().convert()
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
        addr.sin_len = sizeOf<sockaddr_in>().convert()
        addr.sin_port = posix_htons(port.toShort()).toUShort()

//        if (inet_pton(AF_INET, host, addr.sin_addr.ptr) <= 0) {
//
//        }

        val server = gethostbyname(host) ?: throw SocketConnectException("Unknown host \"$host\"")
        addr.sin_addr.s_addr = server.pointed.h_addr_list!![0]!!.reinterpret<UIntVar>().pointed.value

        val r = connect(native.native, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        if (r < 0 && errno!=36) {
            throw SocketConnectException("Can't connect to $host:$port ($r), errno: [$errno]")
        }
    }
}

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int =
        send(socket.native, data.refTo(offset), length.convert(), 0).convert<Int>().convert()

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteDataBuffer, offset: Int, length: Int): Int =
        send(socket.native, data.refTo(offset), length.convert(), 0).convert()

internal actual fun sendSocket(socket: NativeSocketHolder, data: ByteBuffer): Int {
    val r = send(socket.native, data.native + data.position, data.remaining.convert(), 0).convert<Int>()
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
    get() = this.udata!!.asStableRef<SocketSelector.SelectorKeyImpl>().get()

internal actual class NativeEpoll actual constructor(connectionCount: Int) {
    val native = kqueue()
//    val native = epoll_create(connectionCount)

    actual fun free() {
        close(native)
    }

    actual fun wait(list: NativeEpollList, connectionCount: Int, timeout: Int): Int {
        return memScoped {
            val time = if (timeout < 0)
                null
            else {

                val c = alloc<timespec>()
                c.tv_sec = timeout / 1000L
                c.tv_nsec = (timeout - c.tv_sec * 1000) * 1000L
                c.ptr
            }
            kevent(native, null, 0, list.native, connectionCount, time)
        }
    }


    actual fun remove(socket: NativeSocketHolder) {
        memScoped {
            val vv = alloc<kevent>()
            internal_EV_SET(vv.ptr, socket.native, 0, EV_DELETE, 0, 0, NULL)
            kevent(native, vv.ptr, 1, null, 0, null)
        }
    }

    actual fun add(socket: NativeSocketHolder, key: SocketSelector.SelectorKeyImpl): SelfRefKey =
            memScoped {
                val ref = SelfRefKey(key)
                val vv = alloc<kevent>()
                if (key.channel is ServerSocketChannel) {
                    internal_EV_SET(vv.ptr, socket.native, EVFILT_READ, EV_ADD, 0, 0, ref.key)
                } else {
                    internal_EV_SET(vv.ptr, socket.native, 0, EV_ADD, 0, 0, NULL);
                }
                kevent(native, vv.ptr, 1, null, 0, null)
                ref
            }

    actual fun edit(socket: NativeSocketHolder, ref: SelfRefKey, readFlag: Boolean, writeFlag: Boolean) {
        memScoped {
            val event = alloc<kevent>()
            internal_EV_SET(event.ptr, socket.native, 0, EV_ADD, 0, 0, event.ptr);
            var e = 0
            if (readFlag)
                e = e or EVFILT_READ.convert()
            if (writeFlag)
                e = e or EVFILT_WRITE.convert()
            kevent(native, event.ptr, 1, null, 0, null);
        }
    }
}

actual typealias NativeEvent = kevent

internal actual class SelfRefKey(key: SocketSelector.SelectorKeyImpl) : Closeable {
    val key = StableRef.create(key).asCPointer()
    override fun close() {
        key.asStableRef<SocketSelector.SelectorKeyImpl>().dispose()
    }
}

internal actual class NativeEpollList actual constructor(connectionCount: Int) {
    val native = nativeHeap.allocArray<kevent>(connectionCount)//malloc((sizeOf<epoll_event>() * connectionCount).convert())!!.reinterpret<epoll_event>()
    actual fun free() {
        nativeHeap.free(native)
    }

    actual inline operator fun get(index: Int): NativeEvent = native[index]
}

internal actual val NativeEvent.isClosed: Boolean
    get() = flags.convert<Int>() and EV_EOF.convert() != 0

internal actual val NativeEvent.isReadable: Boolean
    get() = filter.convert<Int>() and EVFILT_READ.convert() != 0

internal actual val NativeEvent.isWritable: Boolean
    get() = filter.convert<Int>() and EVFILT_WRITE.convert() != 0

internal actual val NativeEvent.socId: Int
    get() = 0