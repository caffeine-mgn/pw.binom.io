package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.pipe
import platform.posix.read
import pw.binom.collections.LinkedList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.defaultMutableSet
import pw.binom.collections.useName
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.thread.Thread
import kotlin.collections.set

internal val STUB_BYTE = byteArrayOf(1)

class LinuxSelector : AbstractSelector() {

    internal val native = Epoll.create(1000)

    internal val keys = defaultMutableSet<LinuxKey>()
//    internal val keys = TreeSet<LinuxKey>() { a, b -> a.hashCode() - b.hashCode() }

    //    internal val idToKey = HashMap2<Int, LinuxKey>()
    internal val idToKey = defaultMutableMap<Int, LinuxKey>().useName("LinuxSelector")
    private val keyForRemove = LinkedList<Pair<Int, RawSocket>>()
    private val keyForAdd = LinkedList<Pair<Int, LinuxKey>>()
    private val selectLock = SpinLock()
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
                val id = key.hashCode()
                idToKey[id] = key
                key.epollEvent.content {
                    native.add(key.nativeSocket, it)
                }
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForAdd += key.hashCode() to key
        }
    }

    internal fun removeKey(key: LinuxKey, socket: RawSocket) {
        if (selectLock.tryLock()) {
            try {
                idToKey.remove(key.hashCode())
                native.delete(socket, failOnError = false)
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForRemove += key.hashCode() to socket
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

    override fun wakeup() {
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

    override fun select(timeout: Long, selectedEvents: SelectedEvents): Int {
        selectLock.synchronize {
            selectThreadId = Thread.currentThread.id
            try {
                if (keyForAdd.isNotEmpty()) {
                    idToKey.putAll(keyForAdd)
                    keyForAdd.forEach { (_, key) ->
                        key.epollEvent.content {
                            native.add(key.nativeSocket, it)
                        }
                    }
                    keyForAdd.clear()
                }

                if (keyForRemove.isNotEmpty()) {
                    keyForRemove.forEach { (id, socket) ->
                        native.delete(socket, failOnError = false)
                        idToKey.remove(id)?.attachment = null
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
                return eventCount
            } finally {
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
