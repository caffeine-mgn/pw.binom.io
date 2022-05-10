package pw.binom.network

import kotlinx.cinterop.reinterpret
import platform.linux.*
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.plusAssign
import kotlin.collections.set

class LinuxSelector : AbstractSelector() {

    internal val native = epoll_create(1000)
    internal val keys = HashSet<LinuxKey>()

    internal val idToKey = HashMap<Int, LinuxKey>()
    private val keyForRemove = HashSet<Int>()
    private val keysLock = SpinLock()

    internal fun addKey(key: LinuxKey) {
        keysLock.synchronize {
            val id = key.hashCode()
            idToKey[id] = key
        }
    }

    internal fun removeKey(key: LinuxKey) {
        keysLock.synchronize {
            keyForRemove += key.hashCode()
        }
    }

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = LinuxKey(list = native, attachment = attachment, selector = this)
        keys += key
        addKey(key)
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
        addKey(key)
        return key
    }

    override fun select(timeout: Long, selectedEvents: SelectedEvents): Int {
        keysLock.synchronize {
            if (keyForRemove.isNotEmpty()) {
                keyForRemove.forEach {
                    idToKey.remove(it)?.attachment = null
                }
                keyForRemove.clear()
            }
        }
        val eventCount = epoll_wait(
            native,
            selectedEvents.native.reinterpret(),
            minOf(selectedEvents.maxElements, 1000),
            timeout.toInt()
        )
        selectedEvents.eventCount = eventCount
        selectedEvents.selector = this
        return eventCount
    }

    override fun getAttachedKeys(): Collection<Selector.Key> = HashSet(keys)

    override fun close() {
        platform.posix.close(native)
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
