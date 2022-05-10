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

    private val first = AtomicReference<Item<T>?>(null)
    private val last = AtomicReference<Item<T>?>(null)
    val isEmpty
        get() = first.getValue() == null

    fun push(value: T) {
        val item = Item(value)
        item.previous.setValue(last.getValue())
        last.getValue()?.next?.setValue(item)
        last.setValue(item)
        if (first.getValue() == null) {
            first.setValue(item)
        }
    }

    fun pop(): T {
        if (isEmpty) {
            throw NoSuchElementException()
        }
        val item = first.getValue()!!
        if (last.getValue() === item) {
            first.setValue(null)
            last.setValue(null)
        } else {
            first.setValue(item.next.getValue())
            first.getValue()?.previous?.setValue(null)
        }
        return item.value as T
    }
}
