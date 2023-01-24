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

internal val STUB_BYTE = byteArrayOf(1).pin()
private const val MAX_ELEMENTS = 1042

actual class Selector : Closeable {
    private val selectLock = ReentrantLock()
    private val keyForRemove = LinkedList<SelectorKey>()
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
    private val keys = defaultMutableMap<Socket, SelectorKey>()
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

    actual fun attach(socket: Socket): SelectorKey {
        if (socket.blocking) {
            throw IllegalArgumentException("Socket in blocking mode")
        }
        return addKeyLock.synchronize {
            val existKey = keys[socket]
            if (existKey != null) {
                existKey
            } else {
                val key = SelectorKey(selector = this, rawSocket = socket.native)
                key.serverFlag = socket.server
                setEventDataFd(eventMem, socket.native)
                setEventFlags(eventMem, 0, 0)
                epoll.add(socket.native, eventMem)
                keys[socket] = key
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
                key.closed = true
                errorsRemove.add(key)
            }
        }
    }

    internal fun removeKey(key: SelectorKey) {
        if (selectLock.tryLock()) {
            try {
//                println("Selector:: removeKeyNow ${key.rawSocket}")
                epoll.delete(key.rawSocket, true)
                addKeyLock.synchronize {
                    val existKey = keys2.remove(key)
                    if (existKey != null) {
                        nativeKeys.remove(existKey.native)
                        keys.remove(existKey)
                    }
                }
                key.internalClose()
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForRemoveLock.synchronize {
//                println("Selector:: removeKeyLater ${key.event.data.ptr}")
                keyForRemove.add(key)
            }
        }
    }

    private fun deferredRemoveKeys() {
        addKeyLock.synchronize {
            keyForRemoveLock.synchronize {
                keyForRemove.forEachLinked { key ->
                    epoll.delete(key.rawSocket, false)
                    val existKey = keys2.remove(key)
                    if (existKey != null) {
                        nativeKeys.remove(existKey.native)
                        keys.remove(existKey)
                    }
                    key.internalClose()
                }
                keyForRemove.clear()
            }
        }
    }

    private fun cleanupPostProcessing(
        selectedKeys: MutableList<SelectorKey>,
        count: Int
    ) {
        selectedKeys.clear()
        when (selectedKeys) {
            is ArrayList -> {
                selectedKeys.clear()
                selectedKeys.ensureCapacity(count + errorsRemove.size)
            }

            is ArrayList2 -> selectedKeys.prepareCapacity(count + errorsRemove.size)
            else -> selectedKeys.clear()
        }
        var currentNum = 0
        errorsRemoveLock.synchronize {
            errorsRemove.forEach { key ->
                key.internalReadFlags = KeyListenFlags.ERROR
                selectedKeys.add(key)
                key.internalClose()
            }
            errorsRemove.clear()
        }
        while (currentNum < count) {
            val event = getEventFromSelectedList(native, currentNum++)
            val ptr = getEventDataPtr(event)
            if (ptr == null) {
                interruptWakeup()
                continue
            }
            val key = nativeKeys[getEventDataFd(event)] ?: continue
            if (key.closed) {
                key.internalClose()
                continue
            }
            var e = 0
            val listenFlags = getEventFlags(event)
            if (listenFlags and FLAG_ERROR != 0) {
                keyForRemove.add(key)
                key.closed = true
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
            key.internalReadFlags = e
            selectedKeys.add(key)
            key.internalListenFlags = 0
        }
    }

    actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
        selectLock.synchronize {
            interruptWakeup()
            deferredRemoveKeys()
            selectedKeys.lock.synchronize {
//                selectedKeys.errors.clear()
                val eventCount = epoll.select(
                    events = native,
                    timeout = if (timeout.isInfinite()) -1 else timeout.inWholeMilliseconds.toInt(),
                )
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
        if (wakeupFlag.compareAndSet(true, false)) {
            platform.posix.read(pipeRead, STUB_BYTE.addressOf(0), 1.convert())
        }
    }
}
