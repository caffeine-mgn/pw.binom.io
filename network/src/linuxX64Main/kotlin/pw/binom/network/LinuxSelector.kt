package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.AF_INET

class LinuxSelector : AbstractSelector() {

    inner class LinuxKey(
        val list: Int,
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

        override fun resetMode(mode: Int) {
            if (nativeSocket == 0) {
                return
            }
            epollEvent.events = epollCommonToNative(mode.convert()).convert()
            if (nativeSocket != 0) {
                epoll_ctl(list, EPOLL_CTL_MOD, nativeSocket.convert(), epollEvent.ptr)
            }
        }

        override fun close() {
            super.close()
            nativeHeap.free(epollEvent)
            if (nativeSocket != 0) {
                epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), null)
            }
            keys -= this
        }
        override fun toString(): String = "LinuxKey(mode: ${modeToString(listensFlag)}, attachment: $attachment, connected: $connected)"
        fun epollCommonToNative(mode: Int): Int {
            var events = 0
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
            return events
        }
    }

    private var eventCount = 0
    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)
    private val keys = HashSet<LinuxKey>()
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
                if (/*EPOLLHUP in item.events ||*/ EPOLLERR in item.events) {
                    key.resetMode(0)
                    event.key = key
                    event.mode = Selector.EVENT_ERROR
                    return event
                }
                if (EPOLLOUT in item.events) {
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                    key.resetMode(key.listensFlag)
                    return event
                }
                if (EPOLLIN in item.events) {
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.INPUT_READY
                    key.resetMode(key.listensFlag)
                    return event
                }
                if (EPOLLHUP in item.events) {
//                    key.connected = true
                    event.key = key
                    event.mode = 0
                    return event
                }
                throw IllegalStateException("Unknown connection state: ${modeToString(item.events.toInt())}")
            }
            if (EPOLLHUP in item.events) {
                event.key = key
                event.mode = Selector.INPUT_READY
                return event
            }
            val common = epollNativeToCommon(item.events.convert())
            if (common == 0) {
                throw IllegalStateException("Invalid epoll mode: [${modeToString(item.events.convert())}]")
            }
            event.key = key
            event.mode = epollNativeToCommon(item.events.convert())
            if (item.events.toInt() != 0 && event.mode == 0) {
                println("Can't convert ${modeToString(item.events.convert())}")
            }
            return event
        }
    }

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(native, attachment)
        keys += key
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        return key
    }

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(native, attachment)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        keys += key
        return key
    }

    override fun select(timeout: Long, selectedEvents: SelectedEvents): Int {
        selectedEvents as LinuxSelectedEvents
        val eventCount = epoll_wait(
            native,
            selectedEvents.native.reinterpret(),
            minOf(selectedEvents.maxElements, 1000),
            timeout.toInt()
        )
        selectedEvents.eventCount = eventCount
        return eventCount
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
                NSocket(native = item.data.fd, family = AF_INET).close()
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

    override fun getAttachedKeys(): Collection<Selector.Key> = HashSet(keys)

    override fun close() {
        platform.posix.close(native)
        nativeHeap.free(list)
    }
}

internal fun epollNativeToCommon(mode: Int): Int {
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
    if (mode and EPOLLIN != 0) {
        sb.append("EPOLLIN ")
    }
    if (mode and EPOLLPRI != 0) {
        sb.append("EPOLLPRI ")
    }
    if (mode and EPOLLOUT != 0) {
        sb.append("EPOLLOUT ")
    }
    if (mode and EPOLLERR != 0) {
        sb.append("EPOLLERR ")
    }
    if (mode and EPOLLHUP != 0) {
        sb.append("EPOLLHUP ")
    }
    if (mode and EPOLLRDNORM != 0) {
        sb.append("EPOLLRDNORM ")
    }
    if (mode and EPOLLRDBAND != 0) {
        sb.append("EPOLLRDBAND ")
    }
    if (mode and EPOLLWRNORM != 0) {
        sb.append("EPOLLWRNORM ")
    }
    if (mode and EPOLLWRBAND != 0) {
        sb.append("EPOLLWRBAND ")
    }
    if (mode and EPOLLMSG != 0) {
        sb.append("EPOLLMSG ")
    }
    if (mode and EPOLLRDHUP != 0) {
        sb.append("EPOLLRDHUP ")
    }
    if (mode and EPOLLONESHOT != 0) {
        sb.append("EPOLLONESHOT ")
    }
    return sb.toString()
}
