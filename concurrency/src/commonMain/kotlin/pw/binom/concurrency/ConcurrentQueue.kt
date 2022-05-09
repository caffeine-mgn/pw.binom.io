package pw.binom.concurrency

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.atomic.AtomicInt
import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import kotlin.time.Duration

class ConcurrentQueue<T> : AppendableQueue<T> {
    private var _size = AtomicInt(0)
    override val size: Int
        get() = _size.getValue()
    private var top = AtomicReference<Item<T>?>(null)
    private var bottom = AtomicReference<Item<T>?>(null)
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun clear() {
        lock.synchronize {
            _size.setValue(0)
            top.setValue(null)
            bottom.setValue(null)
        }
    }

    private class Item<T>(value: T, next: Item<T>?) {
        val value = AtomicReference(value)
        var next = AtomicReference(next)

        init {
            doFreeze()
        }
    }

    override val isEmpty: Boolean
        get() = _size.getValue() == 0

    fun popBlocked(): T {
        lock.synchronize {
            while (true) {
                val item = top.getValue()
                if (item == null) {
                    condition.await()
                    continue
                }
                top = item.next

                if (bottom == item) {
                    bottom.setValue(null)
                }
                _size--
                return item.value.getValue()
            }
        }
        throw IllegalStateException()
    }

    fun popBlocked(duration: Duration): T? {
        lock.synchronize {
            while (true) {
                val item = top.getValue()
                if (item == null) {
                    if (!condition.await(duration)) {
                        return null
                    }
                    continue
                }
                top = item.next

                if (bottom == item) {
                    bottom.setValue(null)
                }
                _size--
                return item.value.getValue()
            }
        }
        throw IllegalStateException()
    }

    override fun pop(): T =
        lock.synchronize {
            val item = top.getValue() ?: throw NoSuchElementException()

            top = item.next

            if (bottom == item) {
                bottom.setValue(null)
            }
            _size.dec()
            item.value.getValue()
        }

    override fun pop(dist: PopResult<T>) {
        lock.synchronize {
            val item = top.getValue()
            if (item == null) {
                dist.clear()
                return
            }

            top = item.next

            if (bottom == item) {
                bottom.setValue(null)
            }
            _size--
            dist.set(item.value.getValue())
        }
    }

    override fun push(value: T) {
        lock.synchronize {
            val i = Item(value, next = null)

            if (top.getValue() == null) {
                top.setValue(i)
            }

            bottom.getValue()?.next?.setValue(i)
            bottom.setValue(i)
            _size.inc()
            condition.signal()
        }
    }

    override fun peek(): T = lock.synchronize {
        (top.getValue() ?: throw NoSuchElementException()).value.getValue()
    }
}
