package pw.binom.pool

/**
 * Object Pool. [borrow] return object from pool. When object is not need you should call [recycle] for free object
 */
interface ObjectPool<T : Any> {
    /**
     * Return object from pool
     */
    fun borrow(init: ((T) -> Unit)? = null): T

    /**
     * Return object to pool
     */
    fun recycle(value: T)
}
