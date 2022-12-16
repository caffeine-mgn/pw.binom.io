package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import pw.binom.io.Closeable

actual class SelectorKey(actual val selector: Selector, val rawSocket: RawSocket) : AbstractNativeKey(), Closeable {
    actual var attachment: Any? = null
    internal var closed = false
    private var free = false

    private fun commonToEpoll(commonFlags: Int): Int {
        var r = 0
        if (commonFlags and KeyListenFlags.READ != 0) {
            r = r or EPOLLIN or EPOLLHUP or EPOLLERR
        }
        if (commonFlags and KeyListenFlags.WRITE != 0) {
            r = r or EPOLLOUT
        }
        if (commonFlags and KeyListenFlags.ERROR != 0) {
            r = r or EPOLLHUP or EPOLLERR
        }
        if (r != 0) {
            r = r or EPOLLONESHOT
        }
        return r
    }

    internal var internalListenFlags = 0
    actual var listenFlags: Int
        get() = internalListenFlags
        set(value) {
            internalListenFlags = value
            if (!closed) {
                event.events = commonToEpoll(value).convert()
                selector.updateKey(this, event.ptr)
            }
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
