package pw.binom.pool

/**
 * Object Pool. [borrow] return object from pool. When object is not need you should call [recycle] for free object
 */
interface ObjectPool<T : Any> {
    fun borrow(): T

    /**
     * Return object to pool
     */
    fun recycle(value: T)
}

interface PoolObjectFactory<T : Any> : ObjectFactory<T> {
    fun new(pool: ObjectPool<T>): T = new()
}

interface ObjectFactory<T : Any> {
    fun new(): T
    fun free(value: T)
}

/**
 * Return object from pool and apply [init] for config obejct from pool
 */
inline fun <T : Any> ObjectPool<T>.borrow(init: ((T) -> Unit)): T {
    val result = borrow()
    init(result)
    return result
}

inline fun <T : Any, R> ObjectPool<T>.using(action: ((T) -> R)): R {
    val value = borrow()
    try {
        return action(value)
    } finally {
        recycle(value)
    }
}
