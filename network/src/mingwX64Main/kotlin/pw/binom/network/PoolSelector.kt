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
//        if (mode and Selector.EVENT_EPOLLRDHUP != 0) {
//            events = events or EPOLLRDHUP
//        }
//        if (mode and Selector.EVENT_CONNECTED != 0) {
//            events = events or EPOLLPRI or EPOLLWRNORM
//        }

        return events
    }

    actual fun attach(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
//            event.events = commonEventsToWEpoll(mode) or EPOLLERR
            /**
             * Events for detect connect
             */
//            event.events = EPOLLIN or EPOLLOUT or EPOLLERR or EPOLLPRI or EPOLLRDHUP
            event.events = 0xFFFFFFFFu
//            EPOLLIN or
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
            event.data.u32 = commonEventsToWEpoll(mode)
            event.data.u64 = 0uL
            event.data.ptr = attachment
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }
    }

    actual fun detach(socket: NSocket) {
        epoll_ctl(native, EPOLL_CTL_DEL, socket.native, null)
    }

    actual fun edit(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        edit2(
            socket.native, mode, attachment, true
        )
    }

    private fun edit2(socket: SOCKET, mode: Int, attachment: COpaquePointer?, connected: Boolean) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode)
            event.data.sock = socket
            event.data.ptr = attachment
            event.data.u32 = mode.toUInt()
            event.data.u64 = if (connected) 1uL else 0uL
            epoll_ctl(native, EPOLL_CTL_MOD, socket, event.ptr)
        }
    }

    actual fun wait(timeout: Long, func: (attachment: COpaquePointer?, mode: Int) -> Unit): Boolean {
        val eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
        if (eventCount <= 0) {
            return false
        }
        for (i in 0 until eventCount) {
            val item = list[i]
            println("---->mode: ${item.events.toString(2)}  ${aaa(item.events)}")

            /**
             * Connected. Swap listen events to passed whan socket was attached
             */
            if (item.data.u64 == 0uL && item.events and EPOLLOUT != 0u && item.events and EPOLLERR == 0u) {
                println("Connected!")
//                val mode = (item.data.u32.inv() or EPOLLERR).inv()
                edit2(
                    item.data.sock,
                    item.data.u32.convert(),
                    item.data.ptr,
                    true
                )
                func(item.data.ptr, Selector.EVENT_CONNECTED)
                continue
            }
            if (item.data.u64 == 0uL && item.events and EPOLLRDHUP != 0u && item.events and EPOLLERR != 0u) {
                println("Error!!")
//                val mode = (item.data.u32.inv() or EPOLLRDHUP or EPOLLERR).inv()
                epoll_ctl(native, EPOLL_CTL_DEL, item.data.sock, null)
                func(item.data.ptr, Selector.EVENT_ERROR)
                continue
            }
            func(item.data.ptr, wepollEventsToCommon(item.events))
        }
        return true
    }

    override fun close() {
        nativeHeap.free(list)
        epoll_close(native)
    }
}

fun aaa(mode: UInt): String {
    val sb = StringBuilder()
    if (mode and EPOLLIN != 0u)
        sb.append("EPOLLIN ")
    if (mode and EPOLLIN != 0u)
        sb.append("EPOLLPRI ")

    if (mode and EPOLLOUT != 0u)
        sb.append("EPOLLOUT ")

    if (mode and EPOLLERR != 0u)
        sb.append("EPOLLERR ")

    if (mode and EPOLLHUP != 0u)
        sb.append("EPOLLHUP ")

    if (mode and EPOLLRDNORM != 0u)
        sb.append("EPOLLRDNORM ")

    if (mode and EPOLLRDBAND != 0u)
        sb.append("EPOLLRDBAND ")

    if (mode and EPOLLWRNORM != 0u)
        sb.append("EPOLLWRNORM ")
    if (mode and EPOLLWRBAND != 0u)
        sb.append("EPOLLWRBAND ")
    if (mode and EPOLLMSG != 0u)
        sb.append("EPOLLMSG ")
    if (mode and EPOLLRDHUP != 0u)
        sb.append("EPOLLRDHUP ")
    if (mode and EPOLLONESHOT != 0u)
        sb.append("EPOLLONESHOT ")

    return sb.toString()
}