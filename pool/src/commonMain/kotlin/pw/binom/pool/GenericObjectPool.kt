package pw.binom.pool

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
@Suppress("UNCHECKED_CAST")
open class GenericObjectPool<T : Any>(
    val factory: ObjectFactory<T>,
    initCapacity: Int = 16,
    maxSize: Int = Int.MAX_VALUE,
    val minSize: Int = 0,
    val growFactor: Float = 1.5f,
    val shrinkFactor: Float = 0.5f,
    val delayBeforeResize: Duration = 2.minutes,
    val idleInterval: Duration = 2.minutes,
) : ObjectPool<T> {

    private var lastDate = TimeSource.Monotonic.markNow()

    init {
        require(growFactor > 1f) { "factor should be more than 1" }
        require(minSize >= 0) { "minSize should be more or equals 0" }
    }

    private var closed = AtomicBoolean(false)
    var maxSize = maxSize
        private set
    internal var nextCapacity = initCapacity

//    private val threadId = AtomicLong(0)
//    private val count = AtomicInt(0)

//    private fun lock() {
//        if (threadId.getValue() == (ThreadUtils.currentThreadId)) {
//            count.increment()
//        } else {
//            while (true) {
//                if (threadId.compareAndSet(0, ThreadUtils.currentThreadId)) {
//                    break
//                }
// //                sleep(1)
//            }
//            count.increment()
//        }
//    }

    //    private fun unlock() {
//        if (count.getValue() <= 0) {
//            throw IllegalStateException("ReentrantSpinLock is not locked")
//        }
//        if (threadId.getValue() != (ThreadUtils.currentThreadId)) {
//            throw IllegalStateException("Only locking thread can call unlock")
//        }
//        count.decrement()
//        if (count.getValue() == 0) {
//            if (!threadId.compareAndSet(ThreadUtils.currentThreadId, 0)) {
//                throw IllegalStateException("Lock already free")
//            }
//        }
//    }
    private val lock = AtomicBoolean(false)
    private fun <T> synchronize(func: () -> T): T {
        return lock.synchronize(func)
//        lock()
//        return try {
//            func()
//        } finally {
//            unlock()
//        }
    }

    fun updateMaxSize(maxSize: Int) {
        require(maxSize >= minSize) { "maxSize ($maxSize) should be more or equals than minSize ($minSize)" }
        synchronize {
            this.maxSize = maxSize
            if (pool.size > maxSize) {
                internalResizePool(maxSize)
            }
        }
    }

    fun checkTrim() {
        val timeToTrim = lastDate.elapsedNow()
        if (timeToTrim < this.delayBeforeResize) {
//            println("GenericObjectPool::checkTrim. early to trim. time to trim: $timeToTrim, nextCapacity: $nextCapacity")
            return
        }
        synchronize {
//            println("GenericObjectPool::checkTrim. try trim... nextCapacity: $nextCapacity, pool.size: ${pool.size}")
            if (nextCapacity > pool.size) {
                internalCheckGrow()
            } else {
                internalCheckShrink()
            }
        }
    }

    private fun internalResizePool(newSize: Int) {
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
        get() = synchronize {
            pool.size
        }
    private var pool = arrayOfNulls<Any>(initCapacity)
    var size = 0
        private set

    private fun internalCheckGrow() {
        if (lastDate.elapsedNow() < this.delayBeforeResize) {
            return
        }
        val isTimeToGrow = (size * growFactor).roundToInt() <= nextCapacity
        if (isTimeToGrow) {
            internalResizePool(nextCapacity)
            lastDate = TimeSource.Monotonic.markNow()
        }
    }

    private fun internalCheckShrink() {
        if (pool.size == 1) {
            return
        }
        if (lastDate.elapsedNow() < this.delayBeforeResize) {
            return
        }
        val isTimeToShrink = (pool.size * shrinkFactor).roundToInt() >= size
        if (isTimeToShrink) {
            internalResizePool(size)
            lastDate = TimeSource.Monotonic.markNow()
        }
    }

    override fun borrow(owner: Any?): T = synchronize {
        if (size == 0) {
            val obj = factory.allocate(pool = this)
            factory.prepare(value = obj, pool = this, owner = owner)
            obj
        } else {
            val index = --size
            nextCapacity = maxOf(size, nextCapacity - 1)
            val obj = pool[index] as T
            pool[index] = null
            internalCheckShrink()
            factory.prepare(value = obj, pool = this, owner = owner)
            obj
        }
    }

    override fun close() {
        synchronize {
            for (i in 0 until size) {
                factory.deallocate(pool[i] as T, this)
            }
            size = 0
        }
    }

    override fun recycle(value: T) {
        synchronize {
            if (size == pool.size) {
                factory.reset(value, this)
                factory.deallocate(value, this)
                nextCapacity = maxOf(pool.size, nextCapacity + 1)
                internalCheckGrow()
            } else {
                factory.reset(value, this)
                pool[size++] = value
                nextCapacity = size
            }
        }
    }
}

private fun getStack() = Throwable().stackTraceToString().split('\n')
    .map { it.split(' ', '\t').filter { it.isNotBlank() }.joinToString(" ") }.joinToString("->")
