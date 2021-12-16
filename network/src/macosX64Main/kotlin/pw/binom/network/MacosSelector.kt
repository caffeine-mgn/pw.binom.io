package pw.binom.network

import kotlinx.cinterop.*
import platform.darwin.*
import platform.linux.EVFILT_EMPTY
import platform.linux.internal_EV_SET
import platform.posix.ENOENT
import platform.posix.errno
import platform.posix.timespec
import pw.binom.io.IOException

class MacosSelector : AbstractSelector() {

    inner class MacosKey(val native: kevent, val kqueueNative: Int, attachment: Any?, socket: NSocket) :
        AbstractKey(attachment, socket) {
        override fun isSuccessConnected(nativeMode: Int): Boolean =
            EVFILT_WRITE in nativeMode || EV_EOF !in nativeMode

//        private val native = nativeHeap.alloc<kevent>()
        //TODO("nativeMode=${nativeMode.toUInt().toString(2)}")
//            EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

        fun epollCommonToNative(mode: Int): Int {
            var events = 0
            if (!connected && Selector.EVENT_CONNECTED and mode != 0) {
                return EVFILT_WRITE
            }
            if (connected && Selector.OUTPUT_READY and mode != 0) {
                return EVFILT_WRITE
            }
            if (Selector.INPUT_READY and mode != 0) {
                return EVFILT_READ
            }
            return events
        }

        override fun resetMode(mode: Int) {

            val n = epollCommonToNative(mode)
//            println(
//                "${(attachment!!)::class.simpleName}-${attachment!!.hashCode()}-Reset ${
//                    listensFlag.toUInt().toString(2)
//                } -> ${mode.toUInt().toString(2)}"
//            )
            val event = native
            val flags = if (n == 0) EV_DISABLE or EV_CLEAR else EV_ADD or EV_CLEAR or EV_ENABLE// or EV_ONESHOT

            event.filter = (if (n == 0) EVFILT_EMPTY else n).convert()
            event.flags = flags.convert()

//            internal_EV_SET(
//                event.ptr,
//                socket.native,
//                if (n == 0) EVFILT_EMPTY else n,
//                flags,
//                0,
//                0,
//                ptr
//            )
            if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                if (errno == 2) {
                    return
                }
                throw IOException("Can't reset kevent filter. errno: $errno")
            }
        }

        override fun close() {
            super.close()

            val event = native
            internal_EV_SET(event.ptr, socket.native, EVFILT_EMPTY, EV_ADD or EV_CLEAR or EV_DISABLE, 0, 0, null)
            if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                if (errno != ENOENT) {
                    throw IOException("Can't reset kevent filter. errno: $errno")
                }
            }
            internal_EV_SET(event.ptr, socket.native, EVFILT_EMPTY, EV_DELETE or EV_CLEAR, 0, 0, null)
            if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                if (errno != ENOENT) {
                    throw IOException("Can't reset kevent filter. errno: $errno")
                }
            }
            keys -= this
            nativeHeap.free(native)
        }
    }

    private val kqueueNative = kqueue()//epoll_create(1000)!!
    private var eventCount = 0
    init {
        if (kqueueNative == -1) {
            throw IOException("Can't init kqueue. errno: $errno")
        }
    }
    private var c = 0
    private val list = nativeHeap.allocArray<kevent>(1000)//nativeHeap.allocArray<epoll_event>(1000)
    private val keys = HashSet<MacosKey>()
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
            val keyPtr = item.udata!!.asStableRef<MacosKey>()
            val key = keyPtr.get()
            if (key.closed) {
                event.key = key
                event.mode = 0
                return event
            }
            if (!key.connected) {
                if (EV_EOF in item.flags && !key.closed) {
                    key.resetMode(0)
                    event.key = key
                    event.mode = Selector.EVENT_ERROR
                    return event
                }
                if (EVFILT_WRITE == item.filter.toInt()) {
                    key.resetMode(0)
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                    return event
                }
                throw IllegalStateException("Unknown connection state: ${flagsToString(item.filter.toInt())}")
            }
            try {
                val code = when (item.filter.toInt()) {
                    EVFILT_READ -> Selector.INPUT_READY
                    EVFILT_WRITE -> Selector.OUTPUT_READY
                    else -> {
                        0
                    }
                }
                event.key = key
                event.mode = code
                return event
//                func(key, code)
            } finally {
                if (EV_EOF in item.flags && !key.closed) {
                    key.resetMode(0)
                    event.key = key
                    event.mode = Selector.EVENT_ERROR
                    return event
//                    func(key, Selector.EVENT_ERROR)
                }
            }
        }
    }
    override val nativeSelectedKeys: Iterator<NativeKeyEvent>
        get() = nativeSelectedKeys2


    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val event = nativeHeap.alloc<kevent>()
        val key = MacosKey(event, kqueueNative, attachment, socket)
        if (!connectable) {
            key.connected = true
        }

        val n = key.epollCommonToNative(mode)
        val flags = if (n == 0) EV_ADD or EV_DISABLE or EV_CLEAR else EV_ADD or EV_CLEAR or EV_ENABLE
        internal_EV_SET(
            event.ptr,
            socket.native,
            if (n == 0) EVFILT_EMPTY else n,
            flags,
            0,
            0,
            key.ptr
        )
        keys += key
        if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
            throw IOException("Can't set filter of kevent mode to 0b${mode.toUInt().toString(2)}, errno: $errno")
        }
        //key.listensFlag = mode
        return key
    }

    override fun nativeSelect(timeout: Long, func: (AbstractKey, mode: Int) -> Unit): Int {
        val eventCount = memScoped {
            val time = if (timeout < 0)
                null
            else {

                val c = alloc<timespec>()
                c.tv_sec = timeout / 1000L
                c.tv_nsec = (timeout - c.tv_sec * 1000) * 1000L
                c.ptr
            }
            kevent(kqueueNative, null, 0, list, 1000, time)
        }
        if (eventCount <= 0) {
            return 0
        }
        var count = 0
        for (i in 0 until eventCount) {
            val item = list[i]
            val keyPtr = item.udata?.asStableRef<MacosKey>() ?: continue
            val key = keyPtr.get()
            if (key.closed) {
                continue
            }
            count++

            if (!key.connected) {
                if (EV_EOF in item.flags && !key.closed) {
                    key.resetMode(0)
                    func(key, Selector.EVENT_ERROR)
                    continue
                }
                if (EVFILT_WRITE == item.filter.toInt()) {
                    key.resetMode(0)
                    key.connected = true
                    func(key, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
                    continue
                }
                throw IllegalStateException("Unknown connection state")
            }

            try {
                val code = when (item.filter.toInt()) {
                    EVFILT_READ -> Selector.INPUT_READY
                    EVFILT_WRITE -> Selector.OUTPUT_READY
                    else -> {
                        0
                    }
                }
                func(key, code)
            } finally {
                if (EV_EOF in item.flags && !key.closed) {
                    key.resetMode(0)
                    func(key, Selector.EVENT_ERROR)
                }
            }

        }
        return count
    }

    override fun nativeSelect(timeout: Long) {
        eventCount = memScoped {
            val time = if (timeout < 0) {
                null
            } else {
                val c = alloc<timespec>()
                c.tv_sec = timeout / 1000L
                c.tv_nsec = (timeout - c.tv_sec * 1000) * 1000L
                c.ptr
            }
            kevent(kqueueNative, null, 0, list, 1000, time)
        }
    }

    override fun getAttachedKeys(): Collection<Selector.Key> = HashSet(keys)

    override fun close() {
        platform.posix.close(kqueueNative)
        nativeHeap.free(list)
    }
}


private fun epollNativeToCommon(mode: Int): Int {
    var events = 0
    if (EVFILT_READ in mode) {
        events = events or Selector.INPUT_READY
    }
    if (EVFILT_WRITE in mode) {
        events = events or Selector.OUTPUT_READY
    }
    return events
}

actual fun createSelector(): Selector = MacosSelector()

fun modeToString1(mode: Int): String {
    val sb = StringBuilder()
    if (mode and EVFILT_READ != 0)
        sb.append("EVFILT_READ ")
    if (mode and EVFILT_WRITE != 0)
        sb.append("EVFILT_WRITE ")

//    if (mode and EVFILT_EMPTY != 0)
//        sb.append("EVFILT_EMPTY ")

    if (mode and EVFILT_AIO != 0)
        sb.append("EVFILT_AIO ")

    if (mode and EVFILT_VNODE != 0)
        sb.append("EVFILT_VNODE ")

    if (mode and EVFILT_PROC != 0)
        sb.append("EVFILT_PROC ")

//    if (mode and EVFILT_PROCDESC != 0)
//        sb.append("EVFILT_PROCDESC ")

    if (mode and EVFILT_SIGNAL != 0)
        sb.append("EVFILT_SIGNAL ")
    if (mode and EVFILT_TIMER != 0)
        sb.append("EVFILT_TIMER ")
    if (mode and EVFILT_USER != 0)
        sb.append("EVFILT_USER ")

    return sb.toString()
}

fun flagsToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and EV_ADD != 0)
        sb.append("EV_ADD ")
    if (mode and EV_ENABLE != 0)
        sb.append("EV_ENABLE ")

    if (mode and EV_DISABLE != 0)
        sb.append("EV_DISABLE ")

    if (mode and EV_DISPATCH != 0)
        sb.append("EV_DISPATCH ")

    if (mode and EV_DISPATCH2 != 0)
        sb.append("EV_DISPATCH2 ")

    if (mode and EV_DELETE != 0)
        sb.append("EV_DELETE ")

    if (mode and EV_RECEIPT != 0)
        sb.append("EV_RECEIPT ")

    if (mode and EV_ONESHOT != 0)
        sb.append("EV_ONESHOT ")

    if (mode and EV_CLEAR != 0)
        sb.append("EV_CLEAR ")

    if (mode and EV_EOF != 0)
        sb.append("EV_EOF ")

    if (mode and EV_ERROR != 0)
        sb.append("EV_ERROR ")

    return sb.toString()
}