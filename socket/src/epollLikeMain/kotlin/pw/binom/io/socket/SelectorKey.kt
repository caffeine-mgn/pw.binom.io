package pw.binom.io.socket

import platform.common.setEventDataFd
import platform.common.setEventDataPtr
import platform.common.setEventFlags
import pw.binom.io.Closeable

actual class SelectorKey(actual val selector: Selector, val rawSocket: RawSocket) : AbstractNativeKey(), Closeable {
    actual var attachment: Any? = null
    internal var closed = false
    private var free = false
    actual val readFlags: Int
        get() = internalReadFlags
    internal var internalReadFlags = 0
    internal var serverFlag = false

    internal var internalListenFlags = 0

    init {
        NetworkMetrics.incSelectorKey()
        NetworkMetrics.incSelectorKeyAlloc()
    }

    internal fun resetListenFlags(commonFlags: Int) {
        if (!closed) {
            if (free) {
                setEventDataPtr(selector.eventMem, null)
            } else {
                setEventDataFd(selector.eventMem, rawSocket)
            }
            setEventFlags(selector.eventMem, commonFlags, if (serverFlag) 1 else 0)
            selector.updateKey(this, selector.eventMem!!)
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
