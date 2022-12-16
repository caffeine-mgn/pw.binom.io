package pw.binom.io.socket

import pw.binom.collections.defaultMutableList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import java.nio.channels.SelectionKey as JvmSelectionKey

actual class SelectedKeys {
    internal var selectedKeys: List<JvmSelectionKey> = emptyList()
    internal var errors = defaultMutableList<SelectorKey>()
    internal val lock = ReentrantLock()
    private val keyImpl = object : Event {
        var internalKey: SelectorKey? = null
        var internalFlags: Int = 0
        override val key: SelectorKey
            get() = internalKey ?: throw IllegalStateException()
        override val flags: Int
            get() = internalFlags

        override fun toString(): String = buildToString()
    }

    actual fun forEach(func: (Event) -> Unit) {
        lock.withLock {
//            errors.forEach { key ->
//                if (key.isClosed) {
//                    return@forEach
//                }
//            }
            selectedKeys.forEach {
                val key = it.attachment() as SelectorKey
                if (key in errors) {
                    keyImpl.internalKey = key
                    keyImpl.internalFlags = KeyListenFlags.ERROR or KeyListenFlags.READ
                    func(keyImpl)
                    return@forEach
                }
                if (key.isClosed) {
                    return@forEach
                }
//                if (it.isConnectable) {
//                    return@forEach
//                }
                var r = 0
                if (it.isAcceptable || it.isReadable || it.isConnectable) {
                    r = r or KeyListenFlags.READ
                }
                if (it.isWritable || it.isConnectable) {
                    r = r or KeyListenFlags.WRITE
                }

                keyImpl.internalKey = key
                keyImpl.internalFlags = r
                func(keyImpl)
            }
        }
    }

    private class EventImpl(override val key: SelectorKey, override val flags: Int) : Event {
        override fun toString(): String = buildToString()
    }

    actual fun collect(collection: MutableCollection<Event>) {
        forEach {
            collection.add(
                EventImpl(
                    key = it.key,
                    flags = it.flags,
                )
            )
        }
    }

    actual fun toList(): List<Event> {
        val ret = ArrayList<Event>(selectedKeys.size)
        collect(ret)
        return ret
    }
}
