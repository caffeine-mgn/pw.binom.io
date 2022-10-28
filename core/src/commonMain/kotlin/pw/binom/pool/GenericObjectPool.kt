package pw.binom.pool

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
@Suppress("UNCHECKED_CAST")
class GenericObjectPool<T : Any>(
    val factory: ObjectFactory<T>,
    initCapacity: Int = 16,
    maxSize: Int = Int.MAX_VALUE,
    val minSize: Int = 0,
    val growFactor: Float = 1.5f,
    val shrinkFactor: Float = 0.5f,
    val delayBeforeResize: Duration = 2.minutes
) : ObjectPool<T> {

    private var lastDate = TimeSource.Monotonic.markNow()

    init {
        require(growFactor > 1f) { "factor should be more than 1" }
        require(minSize >= 0) { "minSize should be more or equals 0" }
    }

    var maxSize = maxSize
        private set
    internal var nextCapacity = initCapacity

    fun updateMaxSize(maxSize: Int) {
        require(maxSize >= minSize) { "maxSize ($maxSize) should be more or equals than minSize ($minSize)" }
        lock.synchronize {
            this.maxSize = maxSize
            if (pool.size > maxSize) {
                unlockedResizePool(maxSize)
            }
        }
    }

    fun checkTrim() {
        if (lastDate.elapsedNow() < this.delayBeforeResize) {
            return
        }
        if (nextCapacity > capacity) {
            checkGrow()
        } else {
            checkShrink()
        }
    }

    private fun unlockedResizePool(newSize: Int) {
        require(newSize >= minSize) { "newSize ($newSize) should be more or equals newSize ($newSize)" }
        if (newSize == 0) {
            for (i in 0 until size) {
                factory.deallocate(pool[i] as T, this)
            }
            pool = arrayOfNulls<Any>(0)
            size = 0
            nextCapacity = 0
            return
        }
        val tmpNewSize = minOf(newSize, maxSize)
        if (pool.size == tmpNewSize) {
            nextCapacity = tmpNewSize
            return
        }
        pool = if (pool.size > newSize) {
            if (size > newSize) {
                for (i in newSize until pool.size) {
                    factory.deallocate(pool[i] as T, this)
                }
            }
            pool.copyOfRange(fromIndex = 0, toIndex = newSize)
        } else {
            val newPool = arrayOfNulls<Any>(newSize)
            pool.copyInto(destination = newPool, endIndex = size)
            newPool
        }
        if (size > newSize) {
            size = newSize
        }
        if (nextCapacity > newSize) {
            nextCapacity = newSize
        }
    }

    val capacity
        get() = lock.synchronize {
            pool.size
        }
    private var pool = arrayOfNulls<Any>(initCapacity)
    val lock = SpinLock()
    var size = 0
        private set

    private fun checkGrow() {
        if (lastDate.elapsedNow() < this.delayBeforeResize) {
            return
        }
        val isTimeToGrow = (size * growFactor).roundToInt() <= nextCapacity
        if (isTimeToGrow) {
            unlockedResizePool(nextCapacity)
            lastDate = TimeSource.Monotonic.markNow()
        }
    }

    private fun checkShrink() {
        if (pool.size == 1) {
            return
        }
        if (lastDate.elapsedNow() < this.delayBeforeResize) {
            return
        }
        val isTimeToShrink = (pool.size * shrinkFactor).roundToInt() >= size
        if (isTimeToShrink) {
            unlockedResizePool(size)
            lastDate = TimeSource.Monotonic.markNow()
        }
    }

    override fun borrow(): T = lock.synchronize {
        if (size == 0) {
            factory.allocate(this)
        } else {
            val index = --size
            nextCapacity = maxOf(size, nextCapacity - 1)
            val obj = pool[index] as T
            pool[index] = null
            checkShrink()
            obj
        }
    }

    override fun close() {
        lock.synchronize {
            for (i in 0 until size) {
                factory.deallocate(pool[i] as T, this)
            }
            size = 0
        }
    }

    override fun recycle(value: T) {
        lock.synchronize {
            if (size == pool.size) {
                factory.deallocate(value, this)
                nextCapacity = maxOf(pool.size, nextCapacity + 1)
                checkGrow()
            } else {
                pool[size++] = value
                nextCapacity = size
            }
        }
    }
}
