package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.errno

class MingwKey(
    val list: HANDLE,
    attachment: Any?,
    override val selector: MingwSelector,
) : AbstractKey(attachment) {
    private val epollEvent = nativeHeap.alloc<epoll_event>()
    private var nativeSocket: RawSocket = 0

    override fun addSocket(raw: RawSocket) {
        if (nativeSocket != 0) {
            throw IllegalStateException()
        }
        epollEvent.events = epollCommonToNative(listensFlag.convert()).convert()
//        epollEvent.data.ptr = ptr
        epollEvent.data.u32 = this@MingwKey.hashCode().convert()
        epoll_ctl(list, EPOLL_CTL_ADD, raw.convert(), epollEvent.ptr)
        nativeSocket = raw
    }

    override fun removeSocket(raw: RawSocket) {
        if (nativeSocket == raw) {
            epoll_ctl(list, EPOLL_CTL_DEL, raw.convert(), null)
            nativeSocket = 0
            return
        }
        throw IllegalArgumentException("Socket $raw not attached to Selector.Key")
    }

    override fun isSuccessConnected(nativeMode: Int): Boolean =
        EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

    fun epollCommonToNative(mode: Int): Int {
        var events = 0u
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

        return events.toInt()
    }

    override fun toString(): String = "MingwKey(native=${modeToString(listensFlag.toUInt())}, ${generateToString()})"
    override fun resetMode(mode: Int) {
        if (nativeSocket == 0) {
            return
        }

        val epoll = epollCommonToNative(mode.convert())
        epollEvent.events = epoll.convert()
        if (nativeSocket != 0) {
            epoll_ctl(list, EPOLL_CTL_MOD, nativeSocket.convert(), epollEvent.ptr)
        }
    }

    override fun close() {
        super.close()
        nativeHeap.free(epollEvent)
        if (nativeSocket != 0) {
            if (epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), null) != 0) {
                println("epoll_ctl fail. errno: $errno")
            }
        }
        selector.removeKey(this)
        selector.keys -= this
    }
}
