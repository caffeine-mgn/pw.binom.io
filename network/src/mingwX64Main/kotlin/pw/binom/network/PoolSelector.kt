package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import pw.binom.io.Closeable

actual class PoolSelector : Closeable {
    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)

    private fun wepollEventsToCommon(mode: UInt): Int {
        var events = 0
        if (mode and EPOLLIN != 0u) {
            events += Selector.EVENT_EPOLLIN
        }
        if (mode and EPOLLOUT != 0u) {
            events += Selector.EVENT_EPOLLOUT
        }
        if (mode and EPOLLRDHUP != 0u) {
            events += Selector.EVENT_EPOLLRDHUP
        }
        if (mode and EPOLLWRNORM != 0u) {
            events += Selector.EVENT_CONNECTED
        }
        return events
    }

    private fun commonEventsToWEpoll(mode: Int): UInt {
        var events = 0u
        if (mode and Selector.EVENT_EPOLLIN != 0) {
            events = events or EPOLLIN
        }
        if (mode and Selector.EVENT_EPOLLOUT != 0) {
            events = events or EPOLLOUT
        }
        if (mode and Selector.EVENT_EPOLLRDHUP != 0) {
            events = events or EPOLLRDHUP
        }
        if (mode and Selector.EVENT_CONNECTED != 0) {
            events = events or EPOLLPRI or EPOLLWRNORM
        }

        return events
    }

    actual fun attach(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode)
//            event.events = EPOLLIN or
//                    EPOLLPRI or
//                    EPOLLOUT or
//                    EPOLLERR or
//                    EPOLLHUP or
//                    EPOLLRDNORM or
//                    EPOLLRDBAND or
//                    EPOLLWRNORM or
//                    EPOLLWRBAND or
//                    EPOLLMSG or /* Never reported. */
//                    EPOLLRDHUP or
//                    EPOLLONESHOT

            event.data.sock = socket.native
            event.data.ptr = attachment
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }
    }

    actual fun detach(socket: NSocket) {
        epoll_ctl(native, EPOLL_CTL_DEL, socket.native, null)
    }

    actual fun edit(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode)
            event.data.sock = socket.native
            event.data.ptr = attachment
            epoll_ctl(native, EPOLL_CTL_MOD, socket.native, event.ptr)
        }
    }

    actual fun wait(timeout: Long, func: (attachment: COpaquePointer?, mode: Int) -> Unit): Boolean {
        val eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
        if (eventCount <= 0) {
            return false
        }
        for (i in 0 until eventCount) {
            val item = list[i]

            func(item.data.ptr, wepollEventsToCommon(item.events))
        }
        return true
    }

    override fun close() {
        nativeHeap.free(list)
        epoll_close(native)
    }
}