package pw.binom.pool

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize
import pw.binom.io.Closeable

abstract class AbstractFixedSizePool<T : Any>(capacity: Int) : ObjectPool<T>, Closeable {
    protected val pool = arrayOfNulls<Any>(capacity)
    protected val lock = AtomicBoolean(false)
    var size = 0
        protected set
    val capacity
        get() = pool.size

    protected abstract fun new(): T

    @Suppress("UNCHECKED_CAST")
    override fun borrow(): T {
        lock.synchronize {
            if (size == 0) {
                val buffer = new()
                return buffer
            }
            val index = --size
            val result = pool[index]!!
            pool[index] = null
            return result as T
        }
    }

    protected open fun free(value: T) {
        // Do nothing
    }

    protected open fun reset(value: T) {
        // Do nothing
    }

    override fun recycle(value: T) {
        lock.synchronize {
            if (size < pool.size) {
                reset(value)
                pool[size++] = value
            } else {
                free(value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun close() {
        lock.synchronize {
            for (i in pool.indices) {
                val obj = pool[i]
                if (obj != null) {
                    free(obj as T)
                }
                pool[i] = null
            }
            size = 0
        }
    }
}