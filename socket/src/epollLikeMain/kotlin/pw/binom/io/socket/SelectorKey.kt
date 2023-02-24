package pw.binom.io.socket

import platform.common.freeEvent
import platform.common.mallocEvent
import platform.common.setEventDataFd
import platform.common.setEventFlags
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable

actual class SelectorKey(actual val selector: Selector, val socket: Socket) :
    AbstractNativeKey(), Closeable {
    val rawSocket: RawSocket
        get() = socket.native
    actual var attachment: Any? = null
    private var closed = AtomicBoolean(false)
    private var free = AtomicBoolean(false)
    internal val eventMem = mallocEvent()!!
//    internal val event = mallocEvent() ?: TODO("Can't allocate event")

    //    @OptIn(ExperimentalTime::class)
//    var lastActiveTime: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
    actual val readFlags: Int
        get() = internalReadFlags
    internal var internalReadFlags = 0
        set(value) {
            field = value
        }
    internal var serverFlag = false

    internal var internalListenFlags = 0

    init {
        setEventDataFd(eventMem, socket.native)
        setEventFlags(eventMem, 0, 0)
        NetworkMetrics.incSelectorKey()
        NetworkMetrics.incSelectorKeyAlloc()
    }

    private fun resetListenFlags(commonFlags: Int): Boolean {
        if (closed.getValue()) {
            return false
        }
        setEventDataFd(eventMem, rawSocket)
        setEventFlags(eventMem, commonFlags, if (serverFlag) 1 else 0)
        return selector.updateKey(this, eventMem)
    }

    actual fun updateListenFlags(listenFlags: Int): Boolean {
        internalListenFlags = listenFlags
        return resetListenFlags(listenFlags)
    }

    actual val listenFlags: Int
        get() = internalListenFlags

//    internal val event = nativeHeap.alloc<epoll_event>()

    init {
//        event.data.ptr = self.asCPointer()
//        selector.eventMem.data.fd = rawSocket
//        selector.eventMem.events = 0.convert()
    }

    internal fun internalClose() {
        if (!free.compareAndSet(false, true)) {
            return
        }
        freeEvent(eventMem)
        NetworkMetrics.decSelectorKeyAlloc()
//        nativeHeap.free(event)
//        freeEvent(event)
        freeSelfClose()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
//        val stack = Throwable().getStackTrace()
//            .map {
//                it.replace('\t', ' ')
//                    .split(' ')
//                    .map { it.trim() }.filter { it.isNotBlank() }
//                    .joinToString(" ")
//            }
//            .joinToString("<-")
//        println("SelectorKey::close attachment: $attachment, stack: $stack")

        NetworkMetrics.decSelectorKey()
        selector.removeKey(this)
    }

    override fun toString(): String = buildToString()

    actual val isClosed: Boolean
        get() = closed.getValue()
}
