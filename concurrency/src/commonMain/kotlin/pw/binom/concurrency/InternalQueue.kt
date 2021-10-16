package pw.binom.concurrency

import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze

/**
 * Queue for internal using. Work like FIFO. Work without any synchronization.
 * All values pushed into this Queue will freeze.
 */
@Suppress("UNCHECKED_CAST")
internal class InternalQueue<T> {
    private class Item<T>(val value: T?) {
        var next = AtomicReference<Item<T>?>(null)
        var previous = AtomicReference<Item<T>?>(null)

        init {
            doFreeze()
        }
    }

    private var first by AtomicReference<Item<T>?>(null)
    private var last by AtomicReference<Item<T>?>(null)
    val isEmpty
        get() = first == null

    fun push(value: T) {
        val item = Item(value)
        item.previous.value = last
        last?.next?.value = item
        last = item
        if (first == null) {
            first = item
        }
    }

    fun pop(): T {
        if (isEmpty) {
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
        return item.value as T
    }
}