package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import pw.binom.io.Closeable

actual class SelectorKey(actual val selector: Selector, val rawSocket: RawSocket) : AbstractNativeKey(), Closeable {
    actual var attachment: Any? = null
    internal var closed = false
    private var free = false
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
    internal fun resetListenFlags(commonFlags: Int) {
        if (!closed) {
//            if (attachment?.toString()?.contains("TcpServerConnection") == true) {
//                println("SelectorKey:: try update key: ${commonFlagsToString(commonFlags)}, serverFlag: $serverFlag")
//            }
            event.events = commonToEpoll(commonFlags = commonFlags, server = serverFlag).convert()
            selector.updateKey(this, event.ptr)
        }
    }

    actual var listenFlags: Int
        get() = internalListenFlags
        set(value) {
            internalListenFlags = value
            resetListenFlags(value)
        }

    internal val event = nativeHeap.alloc<epoll_event>()

    init {
        event.data.ptr = self.asCPointer()
        event.events = 0.convert()
    }

    internal fun internalClose() {
        if (free) {
            return
        }
        free = true
        nativeHeap.free(event)
        freeSelfClose()
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        selector.removeKey(this)
    }

    override fun toString(): String = buildToString()

    actual val isClosed: Boolean
        get() = closed
}
