package pw.binom.network

import kotlinx.cinterop.reinterpret
import platform.linux.*
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.collections.set

class MingwSelector : AbstractSelector() {
    private val native = epoll_create(1000)!!
    internal val idToKey = HashMap<Int, MingwKey>()
    private val keysLock = SpinLock()
    private val keyForRemove = HashSet<Int>()
    internal val keys = HashSet<MingwKey>()

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

    internal fun addKey(key: MingwKey) {
        keysLock.synchronize {
            val id = key.hashCode()
            idToKey[id] = key
        }
    }

    internal fun removeKey(key: MingwKey) {
        keysLock.synchronize {
            keyForRemove += key.hashCode()
        }
    }

    override fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(list = native, attachment = attachment, selector = this)
        keys += key
        addKey(key)
        if (!connectable) {
            key.connected = true
        }
        key.listensFlag = mode
        return key
    }

    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(list = native, attachment = attachment, selector = this)
        if (!connectable) {
            key.connected = true
        }
        keys += key
        addKey(key)
        key.listensFlag = mode
        return key
    }

    override fun getAttachedKeys(): Collection<Selector.Key> = HashSet(keys)

    override fun close() {
        epoll_close(native)
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
    if (EPOLLHUP in mode) {
        events = events or Selector.EVENT_ERROR
    }
    return events
}

actual fun createSelector(): Selector = MingwSelector()

internal fun modeToString(mode: UInt): String {
    val sb = StringBuilder()
    if (mode and EPOLLIN != 0u) {
        sb.append("EPOLLIN ")
    }
    if (mode and EPOLLPRI != 0u) {
        sb.append("EPOLLPRI ")
    }
    if (mode and EPOLLOUT != 0u) {
        sb.append("EPOLLOUT ")
    }
    if (mode and EPOLLERR != 0u) {
        sb.append("EPOLLERR ")
    }
    if (mode and EPOLLHUP != 0u) {
        sb.append("EPOLLHUP ")
    }
    if (mode and EPOLLRDNORM != 0u) {
        sb.append("EPOLLRDNORM ")
    }
    if (mode and EPOLLRDBAND != 0u) {
        sb.append("EPOLLRDBAND ")
    }
    if (mode and EPOLLWRNORM != 0u) {
        sb.append("EPOLLWRNORM ")
    }
    if (mode and EPOLLWRBAND != 0u) {
        sb.append("EPOLLWRBAND ")
    }
    if (mode and EPOLLMSG != 0u) {
        sb.append("EPOLLMSG ")
    }
    if (mode and EPOLLRDHUP != 0u) {
        sb.append("EPOLLRDHUP ")
    }
    if (mode and EPOLLONESHOT != 0u) {
        sb.append("EPOLLONESHOT ")
    }
    return sb.toString().trim()
}
