package pw.binom.io.socket

actual class SelectedKeys {
  actual constructor() {
  }

  actual fun forEach(func: (Event) -> Unit) {
    TODO()
  }

  actual fun collect(collection: MutableCollection<Event>) {
    TODO()
  }

  actual fun toList(): List<Event> {
    TODO()
  }
}

/*
import kotlinx.cinterop.*
import platform.common.*
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import kotlin.native.internal.createCleaner

actual class SelectedKeys(val maxElements: Int) {
    actual constructor() : this(1024)

    internal val native = nativeHeap.allocArray<epoll_event>(maxElements)
    internal var errors = defaultMutableSet<SelectorKey>()
    internal val lock = ReentrantLock()
    private var count = 0

    @OptIn(ExperimentalStdlibApi::class)
    private var cleaner = createCleaner(native) {
        nativeHeap.free(it)
    }
    private val eventImpl = object : Event {

        var internalKey: SelectorKey? = null
        var internalFlags = 0

        override val key: SelectorKey
            get() = internalKey ?: throw IllegalStateException()
        override val flags: Int
            get() = internalFlags

        override fun toString(): String = buildToString()
    }

    internal fun selected(count: Int) {
        this.count = count
    }

    actual fun forEach(func: (Event) -> Unit) {
        lock.synchronize {
            var currentNum = 0
            while (currentNum < count) {
                val event = native[currentNum++]
                val ptr = event.data.ptr ?: continue
                val key = ptr.asStableRef<SelectorKey>().get()
                if (key in errors) {
                    eventImpl.internalKey = key
                    eventImpl.internalFlags = KeyListenFlags.ERROR or KeyListenFlags.READ
                    func(eventImpl)
                    continue
                }
                if (!key.serverFlag && event.events.toInt() and EPOLLHUP.toInt() != 0) {
                    continue
                }
                var e = 0
                if (event.events.toInt() and EPOLLERR.toInt() != 0 || event.events.toInt() and EPOLLHUP.toInt() != 0) {
                    e = e or KeyListenFlags.ERROR
                }
                if (event.events.toInt() and EPOLLOUT.toInt() != 0) {
                    e = e or KeyListenFlags.WRITE
                }
                if (event.events.toInt() and EPOLLIN.toInt() != 0) {
                    e = e or KeyListenFlags.READ
                }
                eventImpl.internalKey = key
                eventImpl.internalFlags = e
                if (!key.isClosed) {
                    func(eventImpl)
                }
            }
        }
    }

    private class EventImpl(override val key: SelectorKey, override val flags: Int) : Event {
        override fun toString(): String = buildToString()
    }

    actual fun collect(collection: MutableCollection<Event>) {
        forEach {
            collection += EventImpl(
                key = it.key,
                flags = it.flags
            )
        }
    }

    actual fun toList(): List<Event> {
        if (count == 0) {
            return emptyList()
        }
        val output = ArrayList<Event>(count)
        collect(output)
        return output
    }
}
*/
