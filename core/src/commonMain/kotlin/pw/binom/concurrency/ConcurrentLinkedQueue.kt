package pw.binom.concurrency

import kotlin.time.Duration

class ConcurrentLinkedQueue<T> {
    private class Item<T>(val value: T?) {
        var next: Item<T>? = null
        var previous: Item<T>? = null
    }

    private val lock = SpinLock()
    private var first: Item<T>? = null
    private var last: Item<T>? = null
    private var _size = 0L
    val size
        get() = _size

    val isEmpty
        get() = lock.synchronize { first == null }

    val isNotEmpty
        get() = !isEmpty

    fun push(value: T) {
        lock.synchronize {
            val item = Item(value)
            item.previous = last
            last?.next = item
            last = item
            if (first == null) {
                first = item
            }
            _size++
        }
    }

    @Suppress("UNCHECKED_CAST")
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
                first = item.next
                first?.previous = null
            }
            size.dec()
            return item.value as T
        } finally {
            lock.unlock()
        }
    }
}
