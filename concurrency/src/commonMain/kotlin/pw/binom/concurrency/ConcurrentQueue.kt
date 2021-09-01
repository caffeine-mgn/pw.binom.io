package pw.binom.concurrency

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class ConcurrentQueue<T> : AppendableQueue<T> {
    private var _size by AtomicInt(0)
    override val size: Int
        get() = _size
    private var top by AtomicReference<Item<T>?>(null)
    private var bottom by AtomicReference<Item<T>?>(null)
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

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

        init {
            doFreeze()
        }
    }

    override val isEmpty: Boolean
        get() = _size == 0

    @OptIn(ExperimentalTime::class)
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
        throw IllegalStateException()
    }

    @OptIn(ExperimentalTime::class)
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
        throw IllegalStateException()
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