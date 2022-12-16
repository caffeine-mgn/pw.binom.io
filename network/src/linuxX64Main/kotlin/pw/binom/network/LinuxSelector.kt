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

class LinuxSelector : AbstractNativeSelector(), SelectorOld {

    internal val native = Epoll.create(1000)

    internal val keys = defaultMutableSet<LinuxKey>()
    private val keyAccessLock = MySpinLock("keyAccessLock")
    internal fun undefineKey(key: LinuxKey) {
        keyAccessLock.synchronize {
            keys -= key
        }
    }
//    internal val keys = TreeSet<LinuxKey>() { a, b -> a.hashCode() - b.hashCode() }

    //    internal val idToKey = HashMap2<Int, LinuxKey>()
    private val keyForRemove = LinkedList<LinuxKey>()

    //    private val keyForAdd = LinkedList<LinuxKey>()
    private val selectLock = ReentrantLock()
    internal var pipeRead: Int = 0
    internal var pipeWrite: Int = 0

    //    private val addKeyLock = MySpinLock("addKeyLock")
    private val removeKeyLock = MySpinLock("removeKeyLock")

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

        val epollEvent = key.epollEvent
        val r = if (epollEvent != null) {
            native.add(key.nativeSocket, epollEvent.ptr)
        } else {
            null
        }
        println("Socket ${key.nativeSocket} add on call. key: ${key.hashCode()}. thread: ${Thread.currentThread.id} Thread: ${Thread.currentThread.id}, r=$r")
//        return
//        if (selectLock.tryLock()) {
//            println("Added now! Thread: ${Thread.currentThread.id}")
//            try {
//                val epollEvent = key.epollEvent
//                if (epollEvent != null) {
//                    native.add(key.nativeSocket, epollEvent.ptr)
//                }
//                println("Socket ${key.nativeSocket} add on call. key: ${key.hashCode()}. thread: ${Thread.currentThread.id} Thread: ${Thread.currentThread.id}")
//            } finally {
//                selectLock.unlock()
//            }
//        } else {
//            println("Can't addkey on select. select process. Thread: ${Thread.currentThread.id}")
//            addKeyLock.synchronize {
//                keyForAdd += key
//            }
//        }
    }

    internal fun removeKey(key: LinuxKey) {
        if (selectLock.tryLock()) {
            try {
//                println("Remove key now!")
                val r = native.delete(key.nativeSocket, failOnError = false)
                println("Socket ${key.nativeSocket} deleted on call. key: ${key.hashCode()}. Result: $r. thread: ${Thread.currentThread.id}")
                key.internalFree()
            } finally {
                selectLock.unlock()
            }
        } else {
//            println("Remove key later!")
            removeKeyLock.synchronize {
                keyForRemove += key
            }
        }
    }

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractNativeKey {
        val key = LinuxKey(list = native, attachment = attachment, selector = this)
        keyAccessLock.synchronize {
            keys += key
        }
//        addKey(key)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        return key
    }

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractNativeKey {
        val key = LinuxKey(list = native, attachment = attachment, selector = this)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        keyAccessLock.synchronize {
            keys += key
        }
        return key
    }

    private var selectThreadId = 0L
    private val selecting = AtomicBoolean(false)

    override fun wakeup() {
//        if (!selecting.getValue()) {
//            return
//        }
//        if (selectThreadId == 0L || selectThreadId == Thread.currentThread.id) {
//            return
//        }
        println("wakeup!!!!")
        STUB_BYTE.usePinned { p ->
            platform.posix.write(pipeWrite, p.addressOf(0), 1.convert()).convert<Int>()
        }
    }

    internal fun interruptWakeup() {
//        if (selectThreadId == 0L) {
//            println("interruptWakeup cannceled!")
//            return
//        }
        STUB_BYTE.usePinned { p ->
            read(pipeRead, p.addressOf(0), 1.convert())
        }
    }

    private fun processDeregisterQueue() {
        removeKeyLock.synchronize {
//                    println("keyForRemove.size=${keyForRemove.size}")
            if (keyForRemove.isNotEmpty()) {
                keyForRemove.forEach { key ->
//                        val socket = key.socket?.native
                    if (key.nativeSocket != 0) {
                        val deleteKeyResult = native.delete(key.nativeSocket, failOnError = true)
                        when (deleteKeyResult) {
                            Epoll.EpollResult.OK -> {
                                println("Socket ${key.nativeSocket} deleted before selected. key: ${key.hashCode()}. thread: ${Thread.currentThread.id}")
                                // Do nothing
                            }

                            Epoll.EpollResult.INVALID -> {
                                println("Socket ${key.nativeSocket} already closed! key: ${key.hashCode()}. thread: ${Thread.currentThread.id}")
                            }
//                                    Epoll.EpollResult.INVALID -> selectedEvents.addClosedKey(key)
                            else -> println("Error no delete key from poll $deleteKeyResult ${key.nativeSocket}. key: ${key.hashCode()}. thread: ${Thread.currentThread.id}")
                        }
                        key.internalFree()
//                                println("late removed")
                    } else {
//                                println("late socket already null")
                    }
                    key.attachment = null
                }
                keyForRemove.clear()
            }
        }
    }

    override fun select(selectedEvents: SelectedEventsOld, timeout: Long): Int {
        selectLock.synchronize {
            println("Select....")
            selecting.setValue(true)
            selectThreadId = Thread.currentThread.id
            try {
//                addKeyLock.synchronize {
//                    if (keyForAdd.isNotEmpty()) {
//                        keyForAdd.forEach { key ->
//                            val epollEvent = key.epollEvent ?: return@forEach
//                            when (val r = native.add(key.nativeSocket, epollEvent.ptr)) {
//                                Epoll.EpollResult.INVALID -> selectedEvents.addClosedKey(key)
//                                Epoll.EpollResult.OK -> {
//                                    println("Socket ${key.nativeSocket} added before selected (#). key: ${key.hashCode()}. thread: ${Thread.currentThread.id}")
//                                }
//
//                                Epoll.EpollResult.ALREADY_EXIST -> {
//                                    println("Socket ${key.nativeSocket} already added before selected (#). key: ${key.hashCode()}. thread: ${Thread.currentThread.id}")
//                                    // Do nothing
//                                }
//                            }
//                        }
//                        keyForAdd.clear()
//                    }
//                }
                processDeregisterQueue()

                val eventCount = native.select(
                    events = selectedEvents.native.reinterpret(),
                    maxEvents = minOf(selectedEvents.maxElements, 1000),
                    timeout = timeout.toInt()
                )
                processDeregisterQueue()
                selectedEvents.eventCount = eventCount
                selectedEvents.selector = this
                (selectedEvents as? LinuxSelectedEvents)?.internalResetFlags()
                println("Selected! $eventCount.  Thread: ${Thread.currentThread.id}")
                return eventCount
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            } finally {
                selecting.setValue(false)
                selectThreadId = 0L
            }
        }
    }

    override fun getAttachedKeys(): Collection<SelectorOld.Key> = keyAccessLock.synchronize { HashSet(keys) }

    override fun close() {
        platform.posix.close(pipeRead)
        platform.posix.close(pipeWrite)
        native.close()
    }
}

internal fun epollNativeToCommon(mode: Int): Int {
    var events = 0
    if (EPOLLIN in mode) {
        events = events or SelectorOld.INPUT_READY
    }
    if (EPOLLOUT in mode) {
        events = events or SelectorOld.OUTPUT_READY
    }
    if (EPOLLHUP in mode) {
        events = events or SelectorOld.EVENT_ERROR
    }
    return events
}

actual fun createSelector(): SelectorOld = LinuxSelector()

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
