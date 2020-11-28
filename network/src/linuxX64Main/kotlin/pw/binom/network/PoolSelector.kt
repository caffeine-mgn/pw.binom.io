package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import pw.binom.io.Closeable

actual class PoolSelector : Closeable {
    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)

    private fun wepollEventsToCommon(mode: Int): Int {
        var events = 0
        if (mode and EPOLLIN != 0) {
            events = events or Selector.EVENT_EPOLLIN
        }
        if (mode and EPOLLOUT != 0) {
            events = events or Selector.EVENT_EPOLLOUT
        }
        if (mode and EPOLLRDHUP != 0) {
            events = events or Selector.EVENT_EPOLLRDHUP
        }
        if (mode and EPOLLPRI != 0) {
            events += Selector.EVENT_CONNECTED
        }
        return events
    }

    private fun commonEventsToWEpoll(mode: Int): Int {
        println("EPOLLERR=$EPOLLERR")
        println("EPOLLET=$EPOLLET")
        println("EPOLLHUP=$EPOLLHUP")
        println("EPOLLIN=$EPOLLIN")
        println("EPOLLMSG=$EPOLLMSG")
        println("EPOLLONESHOT=$EPOLLONESHOT")
        println("EPOLLOUT=$EPOLLOUT")
        println("EPOLLRDBAND=$EPOLLRDBAND")
        println("EPOLLRDHUP=$EPOLLRDHUP")
        println("EPOLLRDNORM=$EPOLLRDNORM")
        println("EPOLLWAKEUP=$EPOLLWAKEUP")
        println("EPOLLWRBAND=$EPOLLWRBAND")
        println("EPOLLWRNORM=$EPOLLWRNORM")

        var events = 0
        if (mode and Selector.EVENT_EPOLLIN != 0) {
            events += EPOLLIN
        }
        if (mode and Selector.EVENT_EPOLLOUT != 0) {
            events += EPOLLOUT
        }
        if (mode and Selector.EVENT_EPOLLRDHUP != 0) {
            events += EPOLLRDHUP
        }
        if (mode and Selector.EVENT_CONNECTED != 0) {
            events = events or EPOLLPRI
        }
        return events
    }

    actual fun attach(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode).toUInt()
            event.data.fd = socket.native
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
            event.events = commonEventsToWEpoll(mode).toUInt()
            event.data.fd = socket.native
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
            func(item.data.ptr, wepollEventsToCommon(item.events.toInt()))
        }
        return true
    }

    override fun close() {
        nativeHeap.free(list)
        platform.posix.close(native)
    }
}