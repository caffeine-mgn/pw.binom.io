package pw.binom.concurrency

import pw.binom.ObjectTree
import pw.binom.atomic.AtomicReference
import pw.binom.doFreeze
import kotlin.time.Duration
import pw.binom.*
import pw.binom.io.Closeable
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Exchange Point for exchange between different threads
 *
 * Object inside [Exchange] will storage as ObjectTree
 */
class Exchange<T : Any?> : ExchangeInput<T>, ExchangeOutput<T> {

    val input: ExchangeInput<T>
        get() = this

    val output: ExchangeOutput<T>
        get() = this

    class Item<T>(val value: T?) {
        var next = AtomicReference<Item<T>?>(null)
        var previous = AtomicReference<Item<T>?>(null)

        init {
            doFreeze()
        }
    }

    private var first by AtomicReference<Item<T>?>(null)
    private var last by AtomicReference<Item<T>?>(null)
    private val lock = Lock()
    private val condition = lock.newCondition()

    /**
     * Put value into chain. [value] will freeze
     */
    override fun put(value: T) {
        lock.synchronize {
            val item = Item(value)
            item.previous.value = last
            last?.next?.value = item
            last = item
            if (first == null)
                first = item
            condition.signal()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(): T =
        lock.synchronize {
            while (last == null) {
                condition.await()
            }
            val item = last!!
            last = item.previous.value
            last?.next?.value = null
            if (first == item)
                first = null

            val value = item.value as T
            item.next.value = null
            item.previous.value = null
            value
        }

    @OptIn(ExperimentalTime::class)
    override fun get(duration: Duration): T? =
        lock.synchronize {
            val now = TimeSource.Monotonic.markNow()
            while (last == null) {
                if (now.elapsedNow() > duration)
                    return@synchronize null
                condition.await(duration)
            }
            val item = last!!
            last = item.previous.value
            last?.next?.value = null
            if (first == item)
                first = null
            val value = item.value
            item.next.value = null
            item.previous.value = null
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