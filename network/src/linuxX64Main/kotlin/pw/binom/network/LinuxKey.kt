package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*

value class HeapMemory<T : CVariable>(val data: ByteArray) {
    companion object {
        fun <T : CVariable> create(size: Int) = HeapMemory<T>(ByteArray(size))
        inline fun <reified T : CVariable> create() = create<T>(sizeOf<T>().convert())
    }

    inline fun <R> ptr(offset: Int, func: (CPointer<T>) -> R): R = data.usePinned {
        func(it.addressOf(offset).reinterpret())
    }

    inline fun <R> ptr(func: (CPointer<T>) -> R) = ptr(offset = 0, func = func)
}

value class HeapValue<T : CVariable>(val heap: ByteArray) {
    companion object {
        internal inline fun <reified T : CVariable> alloc(): HeapValue<T> =
            HeapValue(ByteArray(sizeOf<T>().convert()))

        internal inline fun <reified T : CVariable> alloc(size: Int): HeapValue<T> =
            HeapValue(ByteArray(sizeOf<T>().convert<Int>() * size))
    }

    inline fun <R> content(func: (CPointer<T>) -> R) = content(offset = 0, func = func)

    inline fun <R> content(offset: Int, func: (CPointer<T>) -> R) = heap.usePinned {
        func(it.addressOf(offset).reinterpret())
    }
}

class LinuxKey(
    val list: Epoll,
    attachment: Any?,
    override val selector: LinuxSelector,
) : AbstractKey(attachment = attachment) {
    internal val epollEvent = HeapValue.alloc<epoll_event>()

    //    internal val epollEvent = nativeHeap.alloc<epoll_event>()
    internal var nativeSocket: RawSocket = 0
    private var selfPtr: StableRef<LinuxKey>? = null

    override fun addSocket(raw: RawSocket) {
        if (nativeSocket != 0) {
            error("Native socket already set")
        }
        val v = StableRef.create(this)
        selfPtr = v
        epollEvent.content {
            it.pointed.events = epollCommonToNative(listensFlag.convert()).convert()
            it.pointed.data.u32 = 38u
            it.pointed.data.u64 = 99u
            it.pointed.data.ptr = v.asCPointer()
        }
        nativeSocket = raw
        selector.addKey(this)
//        selector.wakeup()
    }

    internal fun internalFree() {
        epollEvent.content {
            it.pointed.data.ptr = null
        }
        selfPtr?.dispose()
        selfPtr = null
        nativeSocket = 0
    }

    override fun removeSocket(raw: RawSocket) {
        if (nativeSocket == raw) {
            selector.removeKey(this, raw)
//            selector.wakeup()
            return
        }
//        throw IllegalArgumentException("Socket $raw not attached to Selector.Key $nativeSocket")
    }

    override fun isSuccessConnected(nativeMode: Int): Boolean =
        EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

    override fun resetMode(mode: Int) {
        if (nativeSocket == 0) {
            return
        }

        epollEvent.content {
            it.pointed.events = epollCommonToNative(mode.convert()).convert()
            if (nativeSocket != 0) {
                list.update(socket = nativeSocket, data = it, failOnError = false)
            }
        }
    }

    override fun close() {
        super.close()
        val nativeSocket = nativeSocket
        if (nativeSocket != 0) {
            removeSocket(nativeSocket)
//            if (epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), epollEvent.ptr) != 0) {
//                throw IOException("Can't remove SelectorKey from Selector")
//            }
        }
        selector.keys -= this
    }

    override fun toString(): String =
        "LinuxKey(native=${modeToString(epollCommonToNative(listensFlag))}, connected=$connected, ${generateToString()})"

    fun epollCommonToNative(mode: Int): Int {
        var events = EPOLLONESHOT
        if (Selector.EVENT_ERROR in mode) {
            events = events or EPOLLHUP or EPOLLERR
        }
        if (Selector.INPUT_READY in mode) {
            events = events or EPOLLIN or EPOLLHUP or EPOLLERR
        }
        if (!connected && Selector.EVENT_CONNECTED in mode) {
            events = events or EPOLLOUT
        }
        if (connected && Selector.OUTPUT_READY in mode) {
            events = events or EPOLLOUT
        }
        return events
    }
}
