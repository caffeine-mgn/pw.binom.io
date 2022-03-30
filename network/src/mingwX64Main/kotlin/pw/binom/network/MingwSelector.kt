package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.errno
import platform.windows.GetLastError
import platform.windows.HANDLE

class MingwSelector : AbstractSelector() {

    inner class MingwKey(
        val list: HANDLE,
        attachment: Any?,
    ) : AbstractKey(attachment) {
        private var nativeSocket: RawSocket = 0
        override fun addSocket(raw: RawSocket) {
            println("add $raw $attachment")
            if (nativeSocket != 0) {
                throw IllegalStateException()
            }
            memScoped {
                val event = alloc<epoll_event>()
                event.events = epollCommonToNative(listensFlag.convert()).convert()
                event.data.ptr = ptr
                val c = epoll_ctl(native, EPOLL_CTL_ADD, raw.convert(), event.ptr)
                println("epoll_ctl=$c raw=$raw $errno ${GetLastError()}  attachment=$attachment this=${hashCode()}")
            }
            nativeSocket = raw
        }

        override fun removeSocket(raw: RawSocket) {
            println("remove $raw $attachment")
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
            if (Selector.INPUT_READY in mode) {
                events = events or EPOLLIN
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
                println("No active sockets")
                return
            }
            memScoped {
                val event = alloc<epoll_event>()
                event.events = epollCommonToNative(mode.convert()).convert()
                event.data.ptr = ptr
                if (nativeSocket != 0) {
                    println("set mode s1 $attachment ${mode.toString(2)} ${event.events} $connected")
                    epoll_ctl(list, EPOLL_CTL_MOD, nativeSocket.convert(), event.ptr)
                }
            }
        }

        override fun close() {
            super.close()
            if (nativeSocket != 0) {
                epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), null)
            }
            keys -= this
        }
    }

    private val native = epoll_create(1000)!!
    private val list = nativeHeap.allocArray<epoll_event>(1000)
    override val nativeSelectedKeys
        get() = nativeSelectedKeys2

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(native, attachment)
        key.listensFlag = mode
        keys += key
        if (!connectable) {
            key.connected = true
        }
        return key
    }

    private val keys = HashSet<MingwKey>()

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(native, attachment)
        if (!connectable) {
            key.connected = true
        }
        keys += key

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
            println("---event->${item.events}")
            if (!key.connected) {
                when {
                    EPOLLERR in item.events || EPOLLRDHUP in item.events -> {
                        key.resetMode(0)
                        event.key = key
                        event.mode = 0
                        return event
                    }
                    EPOLLOUT in item.events -> {
                        key.resetMode(0)
                        event.key = key
                        event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                        key.connected = true
                        return event
                    }
                    else -> throw IllegalStateException("Unknown selector key status")
                }
            } else {
                event.key = key
                event.mode = epollNativeToCommon(item.events.convert())
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
    return events
}

actual fun createSelector(): Selector = MingwSelector()

internal fun modeToString(mode: UInt): String {
    val sb = StringBuilder()
    if (mode and EPOLLIN != 0u)
        sb.append("EPOLLIN ")
    if (mode and EPOLLPRI != 0u)
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
