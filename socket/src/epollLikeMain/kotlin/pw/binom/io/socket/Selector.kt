package pw.binom.io.socket

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.pin
import platform.common.*
import platform.common.Event
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.ArrayList2
import pw.binom.collections.LinkedList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import pw.binom.io.IOException
import kotlin.collections.set
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal val STUB_BYTE = byteArrayOf(1).pin()
private const val MAX_ELEMENTS = 1042

actual class Selector : Closeable {
    private val selectLock = ReentrantLock()
    internal val keyForRemove = LinkedList<SelectorKey>()
    private val errorsRemove = defaultMutableSet<SelectorKey>()
    private val errorsRemoveLock = SpinLock()
    private val addKeyLock = SpinLock()
    private val keyForRemoveLock = SpinLock()
    internal val epoll = Epoll.create(1024)
    private val wakeupFlag = AtomicBoolean(false)
    private val native = createSelectedList(MAX_ELEMENTS)!!
    internal val eventMem = mallocEvent()

    internal var pipeRead: Int = 0
    internal var pipeWrite: Int = 0
    private val nativeKeys = defaultMutableMap<RawSocket, SelectorKey>()
    private val keys2 = defaultMutableMap<SelectorKey, Socket>()

    init {
        val fds = createPipe()
        pipeRead = fds.first
        pipeWrite = fds.second
        setBlocking(pipeRead, false)
        setBlocking(pipeWrite, false)

        setEventDataPtr(eventMem, null)
        setEventFlags(eventMem, FLAG_READ, 0)
        val r = epoll.add(pipeRead, eventMem)
        if (r != Epoll.EpollResult.OK) {
            platform.posix.close(pipeRead)
            platform.posix.close(pipeWrite)
            epoll.close()
            throw IOException("Can't init epoll. Can't add default pipe. Status: $r")
        }
    }

    private val eventImpl = object : pw.binom.io.socket.Event {
        var internalKey: SelectorKey? = null
        var internalFlag: Int = 0
        override val key: SelectorKey
            get() = internalKey ?: throw IllegalStateException("key not set")
        override val flags: Int
            get() = internalFlag

        override fun toString(): String = this.buildToString()
    }

    actual fun attach(socket: Socket): SelectorKey {
        if (socket.blocking) {
            throw IllegalArgumentException("Socket in blocking mode")
        }
        return addKeyLock.synchronize {
            val existKey = nativeKeys[socket.native]
            if (existKey != null) {
                existKey
            } else {
                val key = SelectorKey(selector = this, rawSocket = socket.native)
                key.serverFlag = socket.server
                setEventDataFd(eventMem, socket.native)
                setEventFlags(eventMem, 0, 0)
                epoll.add(socket.native, eventMem)
                keys2[key] = socket
                nativeKeys[socket.native] = key
                key
            }
        }
    }

    internal fun updateKey(key: SelectorKey, event: CPointer<Event>) {
        if (!epoll.update(key.rawSocket, event)) {
            epoll.delete(key.rawSocket, false)
            errorsRemoveLock.synchronize {
                removeKey(key)
                errorsRemove.add(key)
            }
        }
    }

    internal fun removeKey(key: SelectorKey) {
        if (selectLock.tryLock()) {
            try {
//                println("Selector:: removeKeyNow ${key.rawSocket}")
                val removeResult = epoll.delete(key.rawSocket, failOnError = false)
                addKeyLock.synchronize {
                    val existKey = keys2.remove(key)
                    if (existKey != null) {
                        nativeKeys.remove(existKey.native)
                    }
//                    println("Remove new $key ${key.identityHashCode()} fd=${key.rawSocket}! $removeResult!")
                }
                key.internalClose()
            } finally {
                selectLock.unlock()
            }
        } else {
//            println("Add for remove later $key ${key.identityHashCode()} fd=${key.rawSocket}!")
            keyForRemoveLock.synchronize {
//                println("Selector:: removeKeyLater ${key.event.data.ptr}")
                keyForRemove.add(key)
            }
        }
    }

    private fun deferredRemoveKeys() {
        addKeyLock.synchronize {
            keyForRemoveLock.synchronize {
//                println("removing key for close. keyForRemove: ${keyForRemove.size}")
                keyForRemove.forEachLinked { key ->
                    val deleteResult = epoll.delete(key.rawSocket, false)
                    val existKey = keys2.remove(key)
                    if (existKey != null) {
                        nativeKeys.remove(existKey.native)
                    }
                    key.internalClose()
//                    println("Remove key from list $key ${key.identityHashCode()} fd=${key.rawSocket}! deleteResult: $deleteResult")
                }
                keyForRemove.clear()
            }
        }
    }

    private fun prepareList(selectedKeys: MutableList<SelectorKey>, count: Int) {
        when (selectedKeys) {
            is ArrayList -> {
                selectedKeys.clear()
                selectedKeys.ensureCapacity(count + errorsRemove.size)
            }

            is ArrayList2 -> selectedKeys.prepareCapacity(count + errorsRemove.size)
            else -> selectedKeys.clear()
        }
    }

    private fun cleanupPostProcessing(
        selectedKeys: MutableList<SelectorKey>,
        count: Int
    ) {
        prepareList(selectedKeys = selectedKeys, count = count)
        cleanupPostProcessing(
            count = count,
        ) { key ->
            selectedKeys.add(key)
        }
    }

    private fun cleanupPostProcessing(
        count: Int,
        func: (SelectorKey) -> Unit,
    ) {
        var currentNum = 0
        errorsRemoveLock.synchronize {
            errorsRemove.forEach { key ->
                key.internalReadFlags = KeyListenFlags.ERROR
                func(key)
                key.internalClose()
            }
            keyForRemove.addAll(errorsRemove)
            errorsRemove.clear()
        }
        while (currentNum < count) {
            val event = getEventFromSelectedList(native, currentNum++)
            val ptr = getEventDataPtr(event)
            if (ptr == null) {
                interruptWakeup()
//                println("Alarm! Event for interopted!")
                continue
            }
            val key = nativeKeys[getEventDataFd(event)]
//            if (key == null) {
//                println("Alarm! Event without kotlin-key! fd=${getEventDataFd(event)}!")
//                continue
//            }
            key ?: continue
            if (key.isClosed) {
//                println("Alarm! Event for closed key! $key ${key.identityHashCode()} fd=${getEventDataFd(event)}!")
                key.internalClose()
                continue
            }
            var e = 0
            val listenFlags = getEventFlags(event)
            if (listenFlags and FLAG_ERROR != 0) {
                keyForRemove.add(key)
                e = e or KeyListenFlags.ERROR
            }

//            if (event.events.toInt() and EPOLLERR.toInt() != 0 || event.events.toInt() and EPOLLHUP.toInt() != 0) {
//                e = e or KeyListenFlags.ERROR
//            }
            if (listenFlags and FLAG_WRITE != 0) {
                e = e or KeyListenFlags.WRITE
            }
            if (listenFlags and FLAG_READ != 0) {
                e = e or KeyListenFlags.READ
            }
            if (listenFlags and FLAG_ONCE != 0) {
                e = e or KeyListenFlags.ONCE
            }
            key.internalReadFlags = e
            func(key)
            if (key.internalListenFlags and KeyListenFlags.ONCE != 0) {
                key.internalListenFlags = 0
            }
        }
    }

    private fun select(timeout: Duration) = epoll.select(
        events = native,
        timeout = if (timeout.isInfinite()) -1 else timeout.inWholeMilliseconds.toInt(),
    )

    actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
        selectLock.synchronize {
            interruptWakeup()
            deferredRemoveKeys()
            selectedKeys.lock.synchronize {
//                selectedKeys.errors.clear()
                val eventCount = select(timeout = timeout)
                cleanupPostProcessing(
                    selectedKeys = selectedKeys.selectedKeys,
//                    native = selectedKeys.native,
                    count = eventCount,
//                    errors = selectedKeys.errors,
                )
                selectedKeys.selected(eventCount)
            }
//            deferredRemoveKeys()
        }
    }

    @OptIn(ExperimentalTime::class)
    actual fun select(timeout: Duration, eventFunc: (pw.binom.io.socket.Event) -> Unit) {
        selectLock.synchronize {
            interruptWakeup()
            deferredRemoveKeys()
            val eventCount = select(timeout = timeout)
//            keyForRemoveLock.synchronize {
//                println("STATUS: nativeKeys.size=${nativeKeys.size} keys2.size=${keys2.size}, keyForRemove: ${keyForRemove.size}, errorsRemove: ${errorsRemove.size} keyForRemove: ${keyForRemove.size} NetworkMetrics: ${NetworkMetrics.selectorKeyCountMetric.value} selectorKeyAllocCountMetric: ${NetworkMetrics.selectorKeyAllocCountMetric.value}")
//            }
            cleanupPostProcessing(
                count = eventCount,
            ) { key ->
//                key.lastActiveTime = TimeSource.Monotonic.markNow()
                eventImpl.internalFlag = key.readFlags
                eventImpl.internalKey = key
                eventFunc(eventImpl)
            }
//            addKeyLock.synchronize {
//                keys2.forEach {
//                    val lastActive = it.key.lastActiveTime.elapsedNow()
//                    if (lastActive > 30.seconds) {
//                        println("->key ${it.key} ${it.key.identityHashCode()} fd=${it.value.native}!, lastActive=$lastActive")
//                    }
//                }
//            }
        }
    }

    override fun close() {
        selectLock.synchronize {
            epoll.delete(pipeRead, false)
            freeEvent(eventMem)
            closeSelectedList(native)
            platform.posix.close(pipeRead)
            platform.posix.close(pipeWrite)
            epoll.close()
        }
    }

    actual fun wakeup() {
        if (wakeupFlag.compareAndSet(false, true)) {
            platform.posix.write(pipeWrite, STUB_BYTE.addressOf(0), 1.convert()).convert<Int>()
        }
    }

    internal fun interruptWakeup() {
        platform.posix.read(pipeRead, STUB_BYTE.addressOf(0), 1.convert())
        wakeupFlag.setValue(false)
    }
}
