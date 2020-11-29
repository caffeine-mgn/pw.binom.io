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
            events = events or Selector2.EVENT_EPOLLIN
        }
        if (mode and EPOLLOUT != 0) {
            events = events or Selector2.EVENT_EPOLLOUT
        }

//        if (mode and EPOLLERR == 0 && mode and EPOLLOUT != 0) {
//            return Selector.EVENT_CONNECTED
//        }
//
//
//        if (mode and EPOLLRDHUP != 0) {
//            events = events or Selector.EVENT_EPOLLRDHUP
//        }
//        println("got $mode -> $events")
//        println("Selector.EVENT_EPOLLRDHUP=${Selector.EVENT_EPOLLRDHUP}")
//        println("Selector.EVENT_EPOLLOUT=${Selector.EVENT_EPOLLOUT}")
//        println("Selector.EVENT_CONNECTED=${Selector.EVENT_CONNECTED}")
//        println("Selector.EVENT_EPOLLIN=${Selector.EVENT_EPOLLIN}")
        return events
    }

    private fun commonEventsToWEpoll(mode: Int): Int {
//        println("EPOLLERR=$EPOLLERR")
//        println("EPOLLET=$EPOLLET")
//        println("EPOLLHUP=$EPOLLHUP")
//        println("EPOLLIN=$EPOLLIN")
//        println("EPOLLMSG=$EPOLLMSG")
//        println("EPOLLONESHOT=$EPOLLONESHOT")
//        println("EPOLLOUT=$EPOLLOUT")
//        println("EPOLLRDBAND=$EPOLLRDBAND")
//        println("EPOLLRDHUP=$EPOLLRDHUP")
//        println("EPOLLRDNORM=$EPOLLRDNORM")
//        println("EPOLLWAKEUP=$EPOLLWAKEUP")
//        println("EPOLLWRBAND=$EPOLLWRBAND")
//        println("EPOLLWRNORM=$EPOLLWRNORM")

        var events = 0
        if (mode and Selector2.EVENT_EPOLLIN != 0) {
            events += EPOLLIN
        }
        if (mode and Selector2.EVENT_EPOLLOUT != 0) {
            events += EPOLLOUT
        }
        if (mode and Selector2.EVENT_EPOLLRDHUP != 0) {
            events += EPOLLRDHUP
        }
        if (mode and Selector2.EVENT_CONNECTED != 0) {
            events = events or EPOLLPRI
        }
        return events
    }

    actual fun attach(socket: NSocket, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            println("Attach $mode -> ${commonEventsToWEpoll(mode)}")
            val event = alloc<epoll_event>()

            event.events = (EPOLLERR or
                    EPOLLHUP or
                    EPOLLIN or
                    EPOLLMSG or
                    EPOLLONESHOT or
                    EPOLLOUT or
                    EPOLLRDBAND or
                    EPOLLRDHUP or
                    EPOLLRDNORM or
                    EPOLLWAKEUP or
                    EPOLLWRBAND or
                    EPOLLWRNORM).convert()
//            event.events = 0xFFFFFFFFu//commonEventsToWEpoll(mode).toUInt()
            event.data.fd = socket.native
            event.data.u32 = commonEventsToWEpoll(mode).convert()
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
            socket.native, mode, attachment
        )
    }

    private fun edit2(socket: Int, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode).convert()
            event.data.fd = socket
            event.data.ptr = attachment
            event.data.u32 = mode.toUInt()
            event.data.u64 = 1uL
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
            if (item.data.u64 == 0uL) {
                if (EPOLLERR in item.events && EPOLLHUP in item.events) {
                    println("Error! ${aaa(item.events.convert())}")
                    func(item.data.ptr, Selector2.EVENT_ERROR)
                    epoll_ctl(native, EPOLL_CTL_DEL, item.data.fd, null)
                    continue
                }

                if (EPOLLERR !in item.events && EPOLLHUP !in item.events && EPOLLOUT in item.events) {
                    edit2(
                        item.data.fd,
                        item.data.u32.convert(),
                        item.data.ptr
                    )
                    println("Connected!  ${aaa(item.events.convert())}")
                    func(item.data.ptr, Selector2.EVENT_CONNECTED or Selector2.EVENT_EPOLLOUT)
                    continue
                }
            }
            println("mode: ${aaa(item.events.convert())}")
//            if (item.data.u32 == 0u) {
//                if (item.events and EPOLLOUT.toUInt() != 0u) {
//                    println("Connected!  status: ${isConnected(item.data.fd)}")
//                }
//            }
            func(item.data.ptr, wepollEventsToCommon(item.events.toInt()))
        }
        return true
    }

    override fun close() {
        nativeHeap.free(list)
        platform.posix.close(native)
    }
}

fun aaa(mode: Int): String {
    val sb = StringBuilder()
    mode in mode
    if (mode and EPOLLIN != 0)
        sb.append("EPOLLIN ")
    if (mode and EPOLLIN != 0)
        sb.append("EPOLLPRI ")

    if (mode and EPOLLOUT != 0)
        sb.append("EPOLLOUT ")

    if (mode and EPOLLERR != 0)
        sb.append("EPOLLERR ")

    if (mode and EPOLLHUP != 0)
        sb.append("EPOLLHUP ")

    if (mode and EPOLLRDNORM != 0)
        sb.append("EPOLLRDNORM ")

    if (mode and EPOLLRDBAND != 0)
        sb.append("EPOLLRDBAND ")

    if (mode and EPOLLWRNORM != 0)
        sb.append("EPOLLWRNORM ")
    if (mode and EPOLLWRBAND != 0)
        sb.append("EPOLLWRBAND ")
    if (mode and EPOLLMSG != 0)
        sb.append("EPOLLMSG ")
    if (mode and EPOLLRDHUP != 0)
        sb.append("EPOLLRDHUP ")
    if (mode and EPOLLONESHOT != 0)
        sb.append("EPOLLONESHOT ")

    return sb.toString()
}



actual fun epollNativeToCommon(mode: Int): Int{
    var events = 0
    if (EPOLLIN in mode) {
        events = events or Selector2.EVENT_EPOLLIN
    }
    if (EPOLLOUT in mode) {
        events = events or Selector2.EVENT_EPOLLOUT
    }
    return events
}