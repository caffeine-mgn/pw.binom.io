package pw.binom.pool

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable

abstract class AbstractFixedSizePool<T : Any>(capacity: Int) : ObjectPool<T>, Closeable {
    protected val pool = arrayOfNulls<Any>(capacity)
    protected val lock = SpinLock()
    var size = 0
        protected set

    protected abstract fun new(): T

    @Suppress("UNCHECKED_CAST")
    override fun borrow(init: ((T) -> Unit)?): T {
        lock.synchronize {
            if (size == 0) {
                return new().also { init?.invoke(it) }
            }
            val index = --size
            val result = pool[index]!!
            pool[index] = null
            init?.invoke(result as T)
            return result as T
        }
    }

    protected open fun free(value: T) {
        // Do nothing
    }

    override fun recycle(value: T) {
        lock.synchronize {
            if (size < pool.size) {
                pool[size++] = value
            } else {
                free(value)
            }
        }
    }

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
