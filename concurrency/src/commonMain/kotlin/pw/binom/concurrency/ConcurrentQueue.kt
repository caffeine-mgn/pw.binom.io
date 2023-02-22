package pw.binom.concurrency

import pw.binom.collections.AppendableQueue
import pw.binom.collections.PopResult
import kotlin.time.Duration

class ConcurrentQueue<T> : AppendableQueue<T> {
    private var _size = 0
    override val size: Int
        get() = lock.synchronize { _size }
    private var top: Item<T>? = null
    private var bottom: Item<T>? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun clear() {
        lock.synchronize {
            _size = 0
            top = null
            bottom = null
        }
    }

    private class Item<T>(val value: T, var next: Item<T>?)

    override val isEmpty: Boolean
        get() = _size == 0

    fun popBlocked(): T {
        lock.synchronize {
            while (true) {
                val item = top
                if (item == null) {
                    condition.await()
                    continue
                }
                top = item.next

                if (bottom == item) {
                    bottom = null
                }
                _size--
                return item.value
            }
        }
    }

    fun popBlocked(duration: Duration): T? {
        lock.synchronize {
            while (true) {
                val item = top
                if (item == null) {
                    if (!condition.await(duration)) {
                        return null
                    }
                    continue
                }
                top = item.next

                if (bottom == item) {
                    bottom = null
                }
                _size--
                return item.value
            }
        }
    }

    override fun pop(): T =
        lock.synchronize {
            val item = top ?: throw NoSuchElementException()

            top = item.next

            if (bottom == item) {
                bottom = null
            }
            _size--
            item.value
        }

    override fun pop(dist: PopResult<T>) {
        lock.synchronize {
            val item = top
            if (item == null) {
                dist.clear()
                return
            }

            top = item.next

            if (bottom == item) {
                bottom = null
            }
            _size--
            dist.set(item.value)
        }
    }

    override fun push(value: T) {
        lock.synchronize {
            val i = Item(value, next = null)

            if (top == null) {
                top = i
            }

            bottom?.next = i
            bottom = i
            _size++
            condition.signal()
        }
    }

    override fun peek(): T = lock.synchronize {
        (top ?: throw NoSuchElementException()).value
    }
}
