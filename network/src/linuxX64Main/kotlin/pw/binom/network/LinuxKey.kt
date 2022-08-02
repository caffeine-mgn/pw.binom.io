package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import kotlin.native.internal.createCleaner

value class HeapMemory<T : CVariable>(val data: ByteArray) {
    companion object {
        fun <T : CVariable> create(size: Int) = HeapMemory<T>(ByteArray(size))
        inline fun <reified T : CVariable> create() = create<T>(sizeOf<T>().convert())
    }

    inline fun ptr(func: (CPointer<T>) -> Unit) {
        data.usePinned {
            func(it.addressOf(0).reinterpret())
        }
    }
}

class LinuxKey(
    val list: Epoll,
    attachment: Any?,
    override val selector: LinuxSelector,
) : AbstractKey(attachment) {
    internal val epollEvent = nativeHeap.alloc<epoll_event>()
    internal var nativeSocket: RawSocket = 0

    override fun addSocket(raw: RawSocket) {
        if (nativeSocket != 0) {
            throw IllegalStateException()
        }
        epollEvent.events = epollCommonToNative(listensFlag.convert()).convert()
//        epollEvent.data.ptr = ptr
        epollEvent.data.u32 = this@LinuxKey.hashCode().convert()
        nativeSocket = raw
        selector.addKey(this)
    }

    override fun removeSocket(raw: RawSocket) {
        if (nativeSocket == raw) {
            selector.removeKey(this, raw)
            nativeSocket = 0
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
        epollEvent.events = epollCommonToNative(mode.convert()).convert()
        if (nativeSocket != 0) {
            list.update(socket = nativeSocket, data = epollEvent.ptr, failOnError = false)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(epollEvent) {
        nativeHeap.free(it)
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
        "LinuxKey(mode: ${modeToString(listensFlag)}, attachment: $attachment, connected: $connected)"

    fun epollCommonToNative(mode: Int): Int {
        var events = 0
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
