package pw.binom.pool

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

/**
 * Object pool. All methods are thread-save
 */
open class DefaultPool<T : Any>(val capacity: Int, val new: (DefaultPool<T>) -> T) : ObjectPool<T> {

    protected val pool = arrayOfNulls<Any>(capacity)
    private val lock = SpinLock()
    var size = 0
        protected set

    @Suppress("UNCHECKED_CAST")
    override fun borrow(init: ((T) -> Unit)?): T {
        lock.synchronize {
            if (size == 0) {
                return new(this).also { init?.invoke(it) }
            }
            val index = --size
            val result = pool[index]!!
            pool[index] = null
            init?.invoke(result as T)
            return result as T
        }
    }

    override fun recycle(value: T) {
        lock.synchronize {
            if (size < capacity) {
                pool[size++] = value
            }
        }
    }
}
