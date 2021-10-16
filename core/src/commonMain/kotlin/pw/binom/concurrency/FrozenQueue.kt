package pw.binom.concurrency

import pw.binom.atomic.AtomicLong
import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FrozenQueue<T> {
    private class Item<T>(val value: T?) {
        var next = AtomicReference<Item<T>?>(null)
        var previous = AtomicReference<Item<T>?>(null)

        init {
            doFreeze()
        }
    }

    private val lock = SpinLock()
    private var first by AtomicReference<Item<T>?>(null)
    private var last by AtomicReference<Item<T>?>(null)
    var size by AtomicLong(0)
        private set

    val isEmpty
        get() = lock.synchronize { first == null }

    val isNotEmpty
        get() = !isEmpty

    fun push(value: T) {
        lock.synchronize {
            val item = Item(value)
            item.previous.value = last
            last?.next?.value = item
            last = item
            if (first == null) {
                first = item
            }
            size++
        }
    }

    fun pop(duration: Duration? = null): T {
        try {
            lock.lock(duration)
            if (size == 0L) {
                throw NoSuchElementException()
            }
            val item = first!!
            if (last === item) {
                first = null
                last = null
            } else {
                first = item.next.value
                first?.previous?.value = null
            }
            size--
            return item.value as T
        } finally {
            lock.unlock()
        }
    }

    init {
        doFreeze()
    }
}