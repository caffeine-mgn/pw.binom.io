package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.EPOLLERR
import platform.linux.EPOLLHUP
import platform.linux.EPOLLIN
import platform.linux.epoll_event
import platform.posix.pipe
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.LinkedList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.forEachLinked
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import pw.binom.io.IOException
import kotlin.time.Duration

internal val STUB_BYTE = byteArrayOf(1).pin()

actual class Selector : Closeable {
    private val selectLock = ReentrantLock()
    private val keyForRemove = LinkedList<SelectorKey>()
    private val errorsRemove = LinkedList<SelectorKey>()
    private val errorsRemoveLock = SpinLock()
    private val addKeyLock = SpinLock()
    private val keyForRemoveLock = SpinLock()
    internal val epoll = Epoll.create(1024)
    private val wakeupFlag = AtomicBoolean(false)

    internal var pipeRead: Int = 0
    internal var pipeWrite: Int = 0
    private val keys = defaultMutableMap<Socket, SelectorKey>()
    private val keys2 = defaultMutableMap<SelectorKey, Socket>()

    init {
        memScoped {
            val fds = allocArray<IntVar>(2)
            pipe(fds)
            pipeRead = fds[0]
            pipeWrite = fds[1]
            setBlocking(pipeRead, false)
            setBlocking(pipeWrite, false)

            val event1 = alloc<epoll_event>()
            event1.data.ptr = null
            event1.events = EPOLLIN.convert()
            val r = epoll.add(pipeRead, event1.ptr)
            if (r != Epoll.EpollResult.OK) {
                platform.posix.close(pipeRead)
                platform.posix.close(pipeWrite)
                epoll.close()
                throw IOException("Can't init epoll. Can't add default pipe.")
            }
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
                epoll.add(socket.native, key.event.ptr)
                keys[socket] = key
                keys2[key] = socket
                key
            }
        }
    }

    internal fun updateKey(key: SelectorKey, event: CPointer<epoll_event>) {
        if (!epoll.update(key.rawSocket, event)) {
//            if (key.attachment?.toString()?.contains("TcpServerConnection") == true) {
//                println("Selector:: tcp server has error on update!!!")
//            }
            epoll.delete(key.rawSocket, false)
            errorsRemoveLock.synchronize {
                key.closed = true
                errorsRemove.addLast(key)
            }
        } else {
//            if (key.attachment?.toString()?.contains("TcpServerConnection") == true) {
//                println("Selector:: tcp server has updated key to ${epollModeToString(event.pointed.events.toInt())}")
//            }
        }
    }

    internal fun removeKey(key: SelectorKey) {
        if (selectLock.tryLock()) {
            try {
                println("Selector:: removeKeyNow ${key.event.data.ptr}")
                epoll.delete(key.rawSocket, true)
                addKeyLock.synchronize {
                    val existKey = keys2.remove(key)
                    if (existKey != null) {
                        keys.remove(existKey)
                    }
                }
                key.internalClose()
            } finally {
                selectLock.unlock()
            }
        } else {
            keyForRemoveLock.synchronize {
                println("Selector:: removeKeyLater ${key.event.data.ptr}")
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
                        keys.remove(existKey)
                    }
                    key.internalClose()
                }
                keyForRemove.clear()
            }
        }
    }

    private fun cleanupPostProcessing(native: CPointer<epoll_event>, errors: MutableSet<SelectorKey>, count: Int) {
        var currentNum = 0
        errorsRemoveLock.synchronize {
            errorsRemove.forEachLinked { key ->
//                if (key.attachment?.toString()?.contains("TcpServerConnection") == true) {
//                    println("Selector:: remove invalid socket and send event")
//                }
                errors += key
                key.internalClose()
            }
            errorsRemove.clear()
        }
        while (currentNum < count) {
            val event = native[currentNum++]
            val ptr = event.data.ptr
            if (ptr == null) {
                interruptWakeup()
                continue
            }
            val key = ptr.asStableRef<SelectorKey>().get()
//            if (key.attachment?.toString()?.contains("TcpServerConnection") == true) {
//                println("Selector:: tcp server happened! Event: ${epollModeToString(event.events.toInt())}")
//            }

            if (key.serverFlag && event.events.toInt() and EPOLLHUP.toInt() != 0 && event.events.toInt() and EPOLLERR.toInt() == 0) {
//                println("Selector:: reset listen keys")
                key.resetListenFlags(key.listenFlags)
                continue
            }

            if (event.events.toInt() and EPOLLERR.toInt() != 0 ||
                event.events.toInt() and EPOLLHUP.toInt() != 0
            ) {
                keyForRemove.add(key)
                key.closed = true
                errors += key
            }
            key.internalListenFlags = 0
        }
    }

    actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
        selectLock.synchronize {
            deferredRemoveKeys()
            selectedKeys.lock.synchronize {
                selectedKeys.errors.clear()
                println("Selector:: selecting...")
                val eventCount = epoll.select(
                    events = selectedKeys.native,
                    maxEvents = selectedKeys.maxElements,
                    timeout = if (timeout.isInfinite()) -1 else timeout.inWholeMilliseconds.toInt(),
                )
                println("Selector:: selected! count $eventCount")
                cleanupPostProcessing(
                    native = selectedKeys.native,
                    count = eventCount,
                    errors = selectedKeys.errors,
                )
                selectedKeys.selected(eventCount)
            }
//            deferredRemoveKeys()
        }
    }

    override fun close() {
        selectLock.synchronize {
            epoll.delete(pipeRead, false)
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
