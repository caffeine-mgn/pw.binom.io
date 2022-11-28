package pw.binom.pool

import pw.binom.io.Closeable

/**
 * Object Pool. [borrow] return object from pool. When object is not need you should call [recycle] for free object
 */
interface ObjectPool<T : Any> : Closeable {
    fun borrow(): T

    /**
     * Return object to pool
     */
    fun recycle(value: T)
}
