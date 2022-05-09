package pw.binom.concurrency

import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Exchange Point for exchange between different threads
 *
 * Object inside [BlockingExchange] will storage as ObjectTree
 */
class BlockingExchange<T : Any?> : BlockingExchangeInput<T>, BlockingExchangeOutput<T> {

    val input: BlockingExchangeInput<T>
        get() = this

    val output: BlockingExchangeOutput<T>
        get() = this

    private class Item<T>(val value: T?) {
        var next = AtomicReference<Item<T>?>(null)
        var previous = AtomicReference<Item<T>?>(null)

        init {
            doFreeze()
        }
    }

    private var first = AtomicReference<Item<T>?>(null)
    private var last = AtomicReference<Item<T>?>(null)
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    /**
     * Put value into chain. [value] will freeze
     */
    override fun put(value: T) {
        lock.synchronize {
            val item = Item(value)
            item.previous.setValue(last.getValue())
            last.getValue()?.next?.setValue(item)
            last.setValue(item)
            if (first.getValue() == null) {
                first.setValue(item)
            }
            condition.signal()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(): T =
        lock.synchronize {
            while (last.getValue() == null) {
                condition.await()
            }
            val item = last.getValue()!!
            last.setValue(item.previous.getValue())
            last.getValue()?.next?.setValue(null)
            if (first == item)
                first.setValue(null)

            val value = item.value as T
            item.next.setValue(null)
            item.previous.setValue(null)
            value
        }

    @OptIn(ExperimentalTime::class)
    override fun get(duration: Duration): T? =
        lock.synchronize {
            val now = TimeSource.Monotonic.markNow()
            while (last.getValue() == null) {
                if (now.elapsedNow() > duration) {
                    return@synchronize null
                }
                condition.await(duration)
            }
            val item = last.getValue()!!
            last.setValue(item.previous.getValue())
            last.getValue()?.next?.setValue(null)
            if (first == item) {
                first.setValue(null)
            }
            val value = item.value
            item.next.setValue(null)
            item.previous.setValue(null)
            value
        }

    val isEmpty
        get() = lock.synchronize {
            first == null
        }

    init {
        doFreeze()
    }
}
