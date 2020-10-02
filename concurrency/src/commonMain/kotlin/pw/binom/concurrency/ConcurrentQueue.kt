package pw.binom.concurrency

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.io.Closeable

class ConcurrentQueue<T> : AppendableQueue<T>, Closeable {
    private var _size by AtomicInt(0)
    override val size: Int
        get() = _size
    private var top by AtomicReference<Item<T>?>(null)
    private var bottom by AtomicReference<Item<T>?>(null)
    private val lock = Lock()

    fun clear() {
        lock.synchronize {
            _size = 0
            top = null
            bottom = null
        }
    }

    private class Item<T>(value: T, next: Item<T>?) {
        val value by AtomicReference(value)
        var next by AtomicReference(next)
    }

    override val isEmpty: Boolean
        get() = _size == 0

    override fun pop(): T {
        lock.synchronize {
            val item = top ?: throw NoSuchElementException()

            top = item.next

            if (bottom == item)
                bottom = null
            _size--
            return item.value
        }
    }

    override fun pop(dist: PopResult<T>) {
        lock.synchronize {
            val item = top
            if (item == null) {
                dist.clear()
                return
            }

            top = item.next

            if (bottom == item)
                bottom = null
            _size--
            dist.set(item.value)
        }
    }

    override fun push(value: T) {
        lock.synchronize {
            val i = Item(value, next = null)

            if (top == null)
                top = i

            bottom?.next = i
            bottom = i
            _size++
        }
    }

    override fun peek(): T = (top ?: throw NoSuchElementException()).value
    override fun close() {
        lock.close()
    }

}