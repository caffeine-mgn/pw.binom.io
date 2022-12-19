@file:JvmName("JvmUtils")

package pw.binom.network

import pw.binom.collections.defaultMutableSet
import pw.binom.io.socket.SelectorKey
import pw.binom.io.socket.addListen
import pw.binom.io.socket.removeListen
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline
value class KeyCollection(private val set: MutableSet<SelectorKey>) {
    constructor() : this(defaultMutableSet())

    fun addKey(key: SelectorKey) = set.add(key)
    fun removeKey(key: SelectorKey) = set.remove(key)

    fun setListensFlag(flags: Int) {
        set.removeIf {
            if (it.isClosed) {
                true
            } else {
                it.listenFlags = flags
                false
            }
        }
    }

    fun wakeup() {
        set.removeIf {
            if (it.isClosed) {
                true
            } else {
                it.selector.wakeup()
                false
            }
        }
    }

    fun addListen(code: Int) {
        set.removeIf {
            if (it.isClosed) {
                true
            } else {
                it.addListen(code)
                false
            }
        }
    }

    fun checkEmpty() {
        if (isEmpty) {
            throw SocketClosedException()
        }
    }

    val isEmpty
        get() = set.isEmpty()

    val isNotEmpty
        get() = set.isNotEmpty()

    fun removeListen(code: Int) {
        set.removeIf {
            if (it.isClosed) {
                true
            } else {
                it.removeListen(code)
                false
            }
        }
    }

    fun close() {
        set.forEach { key ->
            if (!key.isClosed) {
                key.listenFlags = 0
                key.close()
            }
        }
        set.clear()
    }

    override fun toString(): String = set.joinToString(", ")
}

internal fun <T> MutableIterable<T>.removeIf(func: (T) -> Boolean) {
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (func(e)) {
            it.remove()
        }
    }
}

internal fun throwUnixSocketNotSupported(): Nothing =
    throw RuntimeException("Mingw Target not supports Unix Domain Socket")
