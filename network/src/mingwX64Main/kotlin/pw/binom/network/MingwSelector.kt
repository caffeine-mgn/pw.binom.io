package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.windows.HANDLE
import kotlin.native.concurrent.freeze

class MingwSelector : AbstractSelector() {

    class MingwKey(val list: HANDLE, attachment: Any?, socket: NSocket) : AbstractKey(attachment, socket) {
        override fun isSuccessConnected(nativeMode: Int): Boolean =
            EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

        override fun resetMode(mode: Int) {
            memScoped {
                val event = alloc<epoll_event>()
                event.events = mode.convert()
                event.data.ptr = ptr
                epoll_ctl(list, EPOLL_CTL_MOD, socket.native, event.ptr)
            }
        }

        override fun close() {
            super.close()
            epoll_ctl(list, EPOLL_CTL_DEL, socket.native, null)
        }

        init {
//            freeze()
        }
    }

    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)


    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(native, attachment, socket)
        memScoped {
            val event = alloc<epoll_event>()
            event.events = if (connectable) {
                0xFFFFFFFFu
            } else {
                key.connected = true
                mode.convert()
            }
            key.listensFlag = mode
            event.data.ptr = key.ptr
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }
        return key
    }

    override fun nativeSelect(timeout: Long, func: (AbstractKey, mode: Int) -> Unit): Int {
        val eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
        if (eventCount <= 0) {
            return 0
        }
        for (i in 0 until eventCount) {
            val item = list[i]
            val keyPtr = item.data.ptr!!.asStableRef<MingwKey>()
            val key = keyPtr.get()
            func(key, item.events.convert())
        }
        return eventCount
    }

    override fun close() {
        epoll_close(native)
        nativeHeap.free(list)
    }
}

actual fun epollCommonToNative(mode: Int): Int {
    var events = 0u
    if (Selector.INPUT_READY in mode) {
        events = events or EPOLLIN
    }
    if (Selector.OUTPUT_READY in mode) {
        events = events or EPOLLOUT
    }

    return events.toInt()
}

actual fun epollNativeToCommon(mode: Int): Int {
    var events = 0
    if (EPOLLIN in mode) {
        events = events or Selector.INPUT_READY
    }
    if (EPOLLOUT in mode) {
        events = events or Selector.OUTPUT_READY
    }
    return events
}

actual fun createSelector(): Selector = MingwSelector()

fun modeToString(mode: UInt): String {
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