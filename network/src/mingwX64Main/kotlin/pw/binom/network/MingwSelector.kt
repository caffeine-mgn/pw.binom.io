package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.errno
import platform.windows.HANDLE

class MingwSelector : AbstractSelector() {

    inner class MingwKey(
        val list: HANDLE,
        attachment: Any?,
    ) : AbstractKey(attachment) {
        private val epollEvent = nativeHeap.alloc<epoll_event>()
        private var nativeSocket: RawSocket = 0

        override fun addSocket(raw: RawSocket) {
            if (nativeSocket != 0) {
                throw IllegalStateException()
            }
            epollEvent.events = epollCommonToNative(listensFlag.convert()).convert()
            epollEvent.data.ptr = ptr
            epoll_ctl(native, EPOLL_CTL_ADD, raw.convert(), epollEvent.ptr)
            nativeSocket = raw
        }

        override fun removeSocket(raw: RawSocket) {
            if (nativeSocket == raw) {
                epoll_ctl(list, EPOLL_CTL_DEL, raw.convert(), null)
                nativeSocket = 0
                return
            }
            throw IllegalArgumentException("Socket $raw not attached to Selector.Key")
        }

        override fun isSuccessConnected(nativeMode: Int): Boolean =
            EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

        fun epollCommonToNative(mode: Int): Int {
            var events = 0u
            if (Selector.EVENT_ERROR in mode) {
                events = events or EPOLLHUP or EPOLLERR
            }
            if (Selector.INPUT_READY in mode) {
                events = events or EPOLLIN or EPOLLHUP or EPOLLERR
            }
            if (!connected && Selector.EVENT_CONNECTED in mode) {
                events = events or EPOLLOUT
            }
            if (connected && Selector.OUTPUT_READY in mode) {
                events = events or EPOLLOUT
            }

            return events.toInt()
        }

        override fun toString(): String = "MinGW(mode: ${modeToString(listensFlag.toUInt())}, attachment: $attachment)"
        override fun resetMode(mode: Int) {
            if (nativeSocket == 0) {
                return
            }

            val epoll = epollCommonToNative(mode.convert())
            epollEvent.events = epoll.convert()
            if (nativeSocket != 0) {
                epoll_ctl(list, EPOLL_CTL_MOD, nativeSocket.convert(), epollEvent.ptr)
            }
        }
        override fun close() {
            super.close()
            nativeHeap.free(epollEvent)
            if (nativeSocket != 0) {
                if (epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), null) != 0) {
                    println("epoll_ctl fail. errno: $errno")
                }
            }
            keys -= this
        }
    }

    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)
    override fun select(timeout: Long, selectedEvents: SelectedEvents): Int {
        selectedEvents as MingwSelectedEvents
        val eventCount = epoll_wait(
            native,
            selectedEvents.native.reinterpret(),
            minOf(selectedEvents.maxElements, 1000),
            timeout.toInt()
        )
        selectedEvents.eventCount = eventCount
        return eventCount
    }

    override val nativeSelectedKeys
        get() = nativeSelectedKeys2

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(native, attachment)
        keys += key
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        return key
    }

    private val keys = HashSet<MingwKey>()

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(native, attachment)
        if (!connectable) {
            key.connected = true
        }
        keys += key
        key.listensFlag = mode
        return key
    }

    private var eventCount = 0
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
            val keyPtr = item.data.ptr!!.asStableRef<MingwKey>()
            val key = keyPtr.get()
            if (!key.connected) {
                when {
                    EPOLLERR in item.events || EPOLLRDHUP in item.events/* || EPOLLHUP in item.events*/ -> {
                        key.resetMode(0)
                        event.key = key
                        event.mode = 0
                        return event
                    }
                    EPOLLOUT in item.events -> {
                        event.key = key
                        event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                        key.connected = true
                        key.resetMode(key.listensFlag)
                        return event
                    }
                    EPOLLIN in item.events -> {
                        event.key = key
                        event.mode = Selector.EVENT_CONNECTED or Selector.INPUT_READY
                        key.connected = true
                        key.resetMode(key.listensFlag)
                        return event
                    }
                    else -> throw IllegalStateException(
                        "Connect error. Unknown selector key status. epoll status: ${modeToString(item.events)}, ${
                        item.events.toString(2)
                        }"
                    )
                }
            } else {
//                if (EPOLLHUP in item.events) {
//                    NSocket(native = item.data.sock, family = AF_INET).close()
//                    key.close()
//                    event.key = key
//                    event.mode = Selector.EVENT_ERROR
//                    return event
//                }
                event.key = key
                event.mode = epollNativeToCommon(item.events.convert())
                if (item.events.toInt() != 0 && event.mode == 0) {
                    throw Exception("MingwSelector: Can't convert ${modeToString(item.events)} to common")
                }
                return event
            }
        }
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
            if (!key.connected) {
                when {
                    EPOLLERR in item.events || EPOLLRDHUP in item.events -> {
                        key.resetMode(0)
                        func(key, Selector.EVENT_ERROR)
                    }
                    EPOLLOUT in item.events -> {
                        key.resetMode(0)
                        func(key, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
                        key.connected = true
                    }
                    else -> throw IllegalStateException("Unknown selector key status")
                }
            } else {
                func(key, epollNativeToCommon(item.events.convert()))
            }
        }
        return eventCount
    }

    override fun nativeSelect(timeout: Long) {
        nativeSelectedKeys2.reset()
        eventCount = epoll_wait(native, list.reinterpret(), 1000, timeout.toInt())
    }

    override fun getAttachedKeys(): Collection<Selector.Key> = HashSet(keys)

    override fun close() {
        epoll_close(native)
        nativeHeap.free(list)
    }
}

fun epollNativeToCommon(mode: Int): Int {
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

actual fun createSelector(): Selector = MingwSelector()

internal fun modeToString(mode: UInt): String {
    val sb = StringBuilder()
    if (mode and EPOLLIN != 0u) {
        sb.append("EPOLLIN ")
    }
    if (mode and EPOLLPRI != 0u) {
        sb.append("EPOLLPRI ")
    }
    if (mode and EPOLLOUT != 0u) {
        sb.append("EPOLLOUT ")
    }
    if (mode and EPOLLERR != 0u) {
        sb.append("EPOLLERR ")
    }
    if (mode and EPOLLHUP != 0u) {
        sb.append("EPOLLHUP ")
    }
    if (mode and EPOLLRDNORM != 0u) {
        sb.append("EPOLLRDNORM ")
    }
    if (mode and EPOLLRDBAND != 0u) {
        sb.append("EPOLLRDBAND ")
    }
    if (mode and EPOLLWRNORM != 0u) {
        sb.append("EPOLLWRNORM ")
    }
    if (mode and EPOLLWRBAND != 0u) {
        sb.append("EPOLLWRBAND ")
    }
    if (mode and EPOLLMSG != 0u) {
        sb.append("EPOLLMSG ")
    }
    if (mode and EPOLLRDHUP != 0u) {
        sb.append("EPOLLRDHUP ")
    }
    if (mode and EPOLLONESHOT != 0u) {
        sb.append("EPOLLONESHOT ")
    }
    return sb.toString().trim()
}
