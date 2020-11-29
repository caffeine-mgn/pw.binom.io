package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.Reference
import pw.binom.concurrency.asReference
import pw.binom.io.Closeable

actual class PoolSelector : Closeable {
    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)

    private fun wepollEventsToCommon(mode: UInt): Int {
        var events = 0
        if (mode and EPOLLIN != 0u) {
            events += Selector2.EVENT_EPOLLIN
        }
        if (mode and EPOLLOUT != 0u) {
            events += Selector2.EVENT_EPOLLOUT
        }
        return events
    }

    private fun commonEventsToWEpoll(mode: Int): UInt {
        var events = 0u
        if (mode and Selector2.EVENT_EPOLLIN != 0) {
            events = events or EPOLLIN
        }
        if (mode and Selector2.EVENT_EPOLLOUT != 0) {
            events = events or EPOLLOUT
        }

        return events
    }

    actual fun attach(socket: NSocket, mode: Int, attachment: Any?): NativeSelectorKey {
        val attachment = NativeSelectorKey(native, socket, attachment?.asReference())
        memScoped {
            val event = alloc<epoll_event>()
            event.events = 0xFFFFFFFFu
            attachment.mode = mode
            event.data.ptr = attachment.ptr
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }
        return attachment
    }

    private fun edit2(socket: SOCKET, mode: Int, attachment: COpaquePointer?) {
        memScoped {
            val event = alloc<epoll_event>()
            event.events = commonEventsToWEpoll(mode)
            event.data.sock = socket
            event.data.ptr = attachment
            event.data.u32 = mode.toUInt()
            event.data.u64 = 1uL
            epoll_ctl(native, EPOLL_CTL_MOD, socket, event.ptr)
        }
    }

    actual fun wait(timeout: Long, func: (attachment: NativeSelectorKey, mode: Int) -> Unit): Boolean {
        val eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
        if (eventCount <= 0) {
            return false
        }
        for (i in 0 until eventCount) {
            val item = list[i]
            val keyPtr = item.data.ptr!!.asStableRef<NativeSelectorKey>()
            val key = keyPtr.get()
            println("---->mode: ${item.events.toString(2)}  ${aaa(item.events)}, state: ${item.data.u64 == 0uL}")
            if (key.status == 0) {
                if (EPOLLRDHUP in item.events && EPOLLERR in item.events) {
                    println("Error!!")
                    func(key, Selector2.EVENT_ERROR)
                    key.close()
                    continue
                }
                /**
                 * Connected. Swap listen events to passed whan socket was attached
                 */
                if (EPOLLOUT in item.events && EPOLLERR !in item.events) {
                    println("Connected!")
                    key.status = 1
                    func(key, Selector2.EVENT_CONNECTED or Selector2.EVENT_EPOLLOUT)
                    continue
                }
            }
            func(key, wepollEventsToCommon(item.events))
        }
        return true
    }

    override fun close() {
        nativeHeap.free(list)
        epoll_close(native)
    }

    actual class NativeSelectorKey(
        val list: HANDLE,
        actual var socket: NSocket,
        actual val attachment: Reference<Any>?
    ) :
        Closeable {
        actual var status by AtomicInt(0)
        private val _mode = AtomicInt(0)
        actual var mode: Int
            get() = _mode.value
            set(value) {
                _mode.value = value
                memScoped {
                    val event = alloc<epoll_event>()
                    event.events = value.convert()
                    event.data.ptr = ptr
                    epoll_ctl(list, EPOLL_CTL_MOD, socket.native, event.ptr)
                }
            }
        var ptr = StableRef.create(this).asCPointer()
        override fun close() {
            attachment?.close()
            epoll_ctl(list, EPOLL_CTL_DEL, socket.native, null)
            ptr.asStableRef<NativeSelectorKey>().dispose()
        }
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