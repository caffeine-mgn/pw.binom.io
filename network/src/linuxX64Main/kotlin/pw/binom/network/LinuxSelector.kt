package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*

class LinuxSelector : AbstractSelector() {

    class LinuxKey(val list: Int, attachment: Any?, socket: NSocket) : AbstractKey(attachment, socket) {
        override fun isSuccessConnected(nativeMode: Int): Boolean =
            EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

        override fun resetMode(mode: Int) {
            memScoped {
                val event = alloc<epoll_event>()
                event.events = epollCommonToNative(mode).convert()
                event.data.ptr = ptr
                epoll_ctl(list, EPOLL_CTL_MOD, socket.native, event.ptr)
            }
        }

        override fun close() {
            super.close()
            epoll_ctl(list, EPOLL_CTL_DEL, socket.native, null)
        }

        fun epollCommonToNative(mode: Int): Int {
            var events = 0
            if (Selector.INPUT_READY in mode) {
                events = events or EPOLLIN
            }
            if (!connected && Selector.EVENT_CONNECTED in mode) {
                events = events or EPOLLOUT
            }
            if (connected && Selector.OUTPUT_READY in mode) {
                events = events or EPOLLOUT
            }

            return events
        }
    }

    private var eventCount = 0
    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)
    override val nativeSelectedKeys: Iterator<NativeKeyEvent>
        get() = nativeSelectedKeys2
    private val nativeSelectedKeys2 = object : Iterator<NativeKeyEvent> {
        private val event = object : NativeKeyEvent {
            override lateinit var key: AbstractKey
            override var mode: Int = 0
        }
        private var currentNum = 0
        fun reset() {
            currentNum = 0
        }

        override fun hasNext(): Boolean {
            if (currentNum == eventCount) {
                return false
            }
            return true
        }

        override fun next(): NativeKeyEvent {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val item = list[currentNum++]
            val keyPtr = item.data.ptr!!.asStableRef<LinuxKey>()
            val key = keyPtr.get()
            if (!key.connected) {
                if (EPOLLERR in item.events) {
                    key.resetMode(0)
                    event.key = key
                    event.mode = Selector.EVENT_ERROR
                    return event
                }
                if (EPOLLOUT in item.events) {
                    key.resetMode(0)
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                    return event
                }
                throw IllegalStateException("Unknown connection state")
            }
            if (EPOLLHUP in item.events) {
                NSocket(item.data.fd).close()
                key.close()
                event.key = key
                event.mode = 0
                return event
            }
            val common = epollNativeToCommon(item.events.convert())
            if (common == 0) {
                throw IllegalStateException("Invalid epoll mode. Native: [${modeToString(item.events.convert())}]")
            }
            event.key = key
            event.mode = epollNativeToCommon(item.events.convert())
            return event
        }
    }


    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(native, attachment, socket)
        memScoped {
            val event = alloc<epoll_event>()
            if (!connectable) {
                key.connected = true
            }

            key.listensFlag = key.epollCommonToNative(mode)
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
            val keyPtr = item.data.ptr!!.asStableRef<LinuxKey>()
            val key = keyPtr.get()

            if (!key.connected) {
                if (EPOLLERR in item.events) {
                    key.resetMode(0)
                    func(key, Selector.EVENT_ERROR)
                    continue
                }
                if (EPOLLOUT in item.events) {
                    key.resetMode(0)
                    key.connected = true
                    func(key, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
                    continue
                }
                throw IllegalStateException("Unknown connection state")
            }
            if (EPOLLHUP in item.events) {
                NSocket(item.data.fd).close()
                key.close()
            } else {
                val common = epollNativeToCommon(item.events.convert())
                if (common == 0) {
                    throw IllegalStateException("Invalid epoll mode. Native: [${modeToString(item.events.convert())}]")
                }
                func(key, common)
            }
        }
        return eventCount
    }


    override fun nativeSelect(timeout: Long) {
        nativeSelectedKeys2.reset()
        eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
    }

    override fun close() {
        platform.posix.close(native)
        nativeHeap.free(list)
    }
}

private fun epollNativeToCommon(mode: Int): Int {
    var events = 0
    if (EPOLLIN in mode) {
        events = events or Selector.INPUT_READY
    }
    if (EPOLLOUT in mode) {
        events = events or Selector.OUTPUT_READY
    }
    if (EPOLLHUP in mode) {
        events = events or Selector.EVENT_ERROR
    }
    return events
}

actual fun createSelector(): Selector = LinuxSelector()

fun modeToString(mode: Int): String {
    val sb = StringBuilder()
    mode in mode
    if (mode and EPOLLIN != 0)
        sb.append("EPOLLIN ")
    if (mode and EPOLLPRI != 0)
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