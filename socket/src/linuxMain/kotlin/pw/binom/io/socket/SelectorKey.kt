package pw.binom.io.socket

import kotlinx.cinterop.convert
import kotlinx.cinterop.ptr
import platform.linux.*
import pw.binom.io.Closeable

actual class SelectorKey(actual val selector: Selector, val rawSocket: RawSocket) : AbstractNativeKey(), Closeable {
    actual var attachment: Any? = null
    internal var closed = false
    private var free = false
    actual val readFlags: Int
        get() = internalReadFlags
    internal var internalReadFlags = 0
    internal var serverFlag = false

    private fun commonToEpoll(commonFlags: Int, server: Boolean): Int {
        var r = 0
        if (commonFlags and KeyListenFlags.READ != 0) {
            r = r or EPOLLIN or EPOLLERR
            if (!server) {
                r = r or EPOLLHUP
            }
        }
        if (commonFlags and KeyListenFlags.WRITE != 0) {
            r = r or EPOLLOUT
        }
        if (commonFlags and KeyListenFlags.ERROR != 0) {
            r = r or EPOLLERR
            if (!server) {
                r = r or EPOLLHUP
            }
        }
        if (r != 0) {
            r = r or EPOLLONESHOT
        }
//        if (server) {
//            r = r xor EPOLLHUP
//        }
        return r
    }

    internal var internalListenFlags = 0

    init {
        NetworkMetrics.incSelectorKey()
        NetworkMetrics.incSelectorKeyAlloc()
    }

    internal fun resetListenFlags(commonFlags: Int) {
        if (!closed) {
            if (free) {
                selector.eventMem.data.ptr = null
            }
            selector.eventMem.data.fd = rawSocket
            selector.eventMem.events = commonToEpoll(commonFlags = commonFlags, server = serverFlag).convert()
            selector.updateKey(this, selector.eventMem.ptr)
        }
    }

    actual var listenFlags: Int
        get() = internalListenFlags
        set(value) {
            internalListenFlags = value
            resetListenFlags(value)
        }

//    internal val event = nativeHeap.alloc<epoll_event>()

    init {
//        event.data.ptr = self.asCPointer()
//        selector.eventMem.data.fd = rawSocket
//        selector.eventMem.events = 0.convert()
    }

    internal fun internalClose() {
        if (free) {
            return
        }
        NetworkMetrics.decSelectorKeyAlloc()
        free = true
//        nativeHeap.free(event)
        freeSelfClose()
    }

    override fun close() {
        if (closed) {
            return
        }
        NetworkMetrics.decSelectorKey()
        closed = true
        selector.removeKey(this)
    }

    override fun toString(): String = buildToString()

    actual val isClosed: Boolean
        get() = closed
}
