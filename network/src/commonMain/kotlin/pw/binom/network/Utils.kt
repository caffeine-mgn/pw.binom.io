@file:JvmName("JvmUtils")

package pw.binom.network

import pw.binom.Environment
import pw.binom.collections.defaultMutableSet
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

expect val Short.hton: Short
expect val Short.ntoh: Short

expect val Int.hton: Int
expect val Int.ntoh: Int

expect val Long.hton: Long
expect val Long.ntoh: Long

expect val Environment.isBigEndian2: Boolean

@JvmInline
value class KeyCollection(private val set: MutableSet<Selector.Key>) {
    constructor() : this(defaultMutableSet())

    fun addKey(key: Selector.Key) = set.add(key)
    fun removeKey(key: Selector.Key) = set.remove(key)

    fun setListensFlag(flags: Int) {
        set.removeIf {
            if (it.closed) {
                true
            } else {
                it.listensFlag = flags
                false
            }
        }
    }

    fun wakeup() {
        set.removeIf {
            if (it.closed) {
                true
            } else {
                it.selector.wakeup()
                false
            }
        }
    }

    fun addListen(code: Int) {
        set.removeIf {
            if (it.closed) {
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
            if (it.closed) {
                true
            } else {
                it.removeListen(code)
                false
            }
        }
    }

    fun close() {
        set.forEach { key ->
            if (!key.closed) {
                key.listensFlag = 0
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
