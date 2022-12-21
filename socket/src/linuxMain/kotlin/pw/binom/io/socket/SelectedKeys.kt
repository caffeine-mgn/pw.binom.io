package pw.binom.io.socket

import pw.binom.collections.defaultMutableList
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize

actual class SelectedKeys actual constructor() {

    //    internal val native = nativeHeap.allocArray<epoll_event>(maxElements)
//    internal var errors = defaultMutableSet<SelectorKey>()
    internal val lock = ReentrantLock()
    internal val selectedKeys = defaultMutableList<SelectorKey>()
//    private var count = 0

    //    @OptIn(ExperimentalStdlibApi::class)
//    private var cleaner = createCleaner(native) {
//        nativeHeap.free(it)
//    }
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
//        this.count = count
    }

    actual fun forEach(func: (Event) -> Unit) {
        lock.synchronize {
            var i = 0
            while (i < selectedKeys.size) {
                val k = selectedKeys[i]
                eventImpl.internalKey = k
                eventImpl.internalFlags = k.readFlags
                func(eventImpl)
                i++
            }
            /*
            return
            var currentNum = 0
            while (currentNum < count) {
                val event = native[currentNum++]
                val ptr = event.data.ptr ?: continue
                println("SelectedKeys:: processing $ptr")
                val key = ptr.asStableRef<SelectorKey>().get()
                if (key in errors) {
                    eventImpl.internalKey = key
                    eventImpl.internalFlags = KeyListenFlags.ERROR or KeyListenFlags.READ
                    func(eventImpl)
                    continue
                }
                if (event.events.toInt() and EPOLLERR.toInt() == 0 && event.events.toInt() and EPOLLHUP.toInt() != 0 && key.serverFlag) {
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
                key.internalReadFlags = e
                eventImpl.internalKey = key
                eventImpl.internalFlags = e
                if (!key.isClosed) {
                    func(eventImpl)
                }
            }
            */
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
        if (selectedKeys.isEmpty()) {
            return emptyList()
        }

        val output = ArrayList<Event>(selectedKeys.size)
        collect(output)
        return output
    }
}
