package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.pipe
import platform.posix.read
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.LinkedList
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.thread.Thread

internal val STUB_BYTE = byteArrayOf(1)

class LinuxSelector : AbstractSelector() {

    internal val native = Epoll.create(1000)

    internal val keys = defaultMutableSet<LinuxKey>()
//    internal val keys = TreeSet<LinuxKey>() { a, b -> a.hashCode() - b.hashCode() }

    //    internal val idToKey = HashMap2<Int, LinuxKey>()
    private val keyForRemove = LinkedList<LinuxKey>()
    private val keyForAdd = LinkedList<LinuxKey>()
    private val selectLock = ReentrantLock()
    internal var pipeRead: Int = 0
    internal var pipeWrite: Int = 0

    init {
        memScoped {
            val fds = allocArray<IntVar>(2)
            pipe(fds)
            pipeRead = fds[0]
            pipeWrite = fds[1]
            setBlocking(pipeRead, false)
            setBlocking(pipeWrite, false)

            val event1 = alloc<epoll_event>()
            event1.data.fd = pipeRead
            event1.data.u32 = 99u
            event1.data.u64 = 77u
            event1.data.ptr = null
            event1.events = EPOLLIN.convert()
            native.add(pipeRead, event1.ptr)

//            val event2 = alloc<epoll_event>()
//            event2.data.fd = pipeWrite
//            event2.events = EPOLLIN.convert()
//            native.add(pipeWrite, event2.ptr)
        }
    }

    internal fun addKey(key: LinuxKey) {
        if (selectLock.tryLock()) {
            try {
                key.epollEvent.content {
                    native.add(key.nativeSocket, it)
                }
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForAdd += key
        }
    }

    internal fun removeKey(key: LinuxKey, socket: RawSocket) {
        if (selectLock.tryLock()) {
            try {
                native.delete(socket, failOnError = false)
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForRemove += key
        }
    }

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(list = native, attachment = attachment, selector = this)
        keys += key
//        addKey(key)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        return key
    }

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(list = native, attachment = attachment, selector = this)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        keys += key
        return key
    }

    private var selectThreadId = 0L
    private val selecting = AtomicBoolean(false)

    override fun wakeup() {
        if (!selecting.getValue()) {
            return
        }
        if (selectThreadId == 0L || selectThreadId == Thread.currentThread.id) {
            return
        }
        STUB_BYTE.usePinned { p ->
            platform.posix.write(pipeWrite, p.addressOf(0), 1.convert()).convert<Int>()
        }
    }

    internal fun interruptWakeup() {
        if (selectThreadId == 0L) {
            return
        }
        STUB_BYTE.usePinned { p ->
            read(pipeRead, p.addressOf(0), 1.convert())
        }
    }

    override fun select(selectedEvents: SelectedEvents, timeout: Long): Int {
        selectLock.synchronize {
            selecting.setValue(true)
            selectThreadId = Thread.currentThread.id
            try {
                if (keyForAdd.isNotEmpty()) {
                    keyForAdd.forEach { key ->
                        key.epollEvent.content {
                            native.add(key.nativeSocket, it)
                        }
                    }
                    keyForAdd.clear()
                }

                if (keyForRemove.isNotEmpty()) {
                    keyForRemove.forEach { key ->
                        val socket = key.socket?.native
                        if (socket != null) {
                            native.delete(socket, failOnError = false)
                        }
                        key.attachment = null
                    }
                    keyForRemove.clear()
                }
                val eventCount = epoll_wait(
                    native.raw,
                    selectedEvents.native.reinterpret(),
                    minOf(selectedEvents.maxElements, 1000),
                    timeout.toInt()
                )
                selectedEvents.eventCount = eventCount
                selectedEvents.selector = this
                (selectedEvents as? LinuxSelectedEvents)?.internalResetFlags()
                return eventCount
            } finally {
                selecting.setValue(false)
                selectThreadId = 0L
            }
        }
    }

    override fun getAttachedKeys(): Collection<Selector.Key> = keys

    override fun close() {
        platform.posix.close(pipeRead)
        platform.posix.close(pipeWrite)
        native.close()
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
    fun append(text: String) {
        if (sb.isNotEmpty()) {
            sb.append(" ")
        }
        sb.append(text)
    }
    if (mode and EPOLLIN != 0) {
        append("EPOLLIN")
    }
    if (mode and EPOLLPRI != 0) {
        append("EPOLLPRI")
    }
    if (mode and EPOLLOUT != 0) {
        append("EPOLLOUT")
    }
    if (mode and EPOLLERR != 0) {
        append("EPOLLERR")
    }
    if (mode and EPOLLHUP != 0) {
        append("EPOLLHUP")
    }
    if (mode and EPOLLRDNORM != 0) {
        append("EPOLLRDNORM")
    }
    if (mode and EPOLLRDBAND != 0) {
        append("EPOLLRDBAND")
    }
    if (mode and EPOLLWRNORM != 0) {
        append("EPOLLWRNORM")
    }
    if (mode and EPOLLWRBAND != 0) {
        append("EPOLLWRBAND")
    }
    if (mode and EPOLLMSG != 0) {
        append("EPOLLMSG")
    }
    if (mode and EPOLLRDHUP != 0) {
        append("EPOLLRDHUP")
    }
    if (mode and EPOLLONESHOT != 0) {
        append("EPOLLONESHOT")
    }
    return sb.toString()
}
