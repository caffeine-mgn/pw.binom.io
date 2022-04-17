package pw.binom.pool

/**
 * Object Pool. [borrow] return object from pool. When object is not need you should call [recycle] for free object
 */
interface ObjectPool<T : Any> {
    fun borrow(): T = borrow(null)

    /**
     * Return object to pool
     */
    fun recycle(value: T)
}

interface ObjectManger<T : Any> {
    fun new(pool: ObjectPool<T>): T
    fun free(value: T)
}

/**
 * Return object from pool
 */
fun <T : Any> ObjectPool<T>.borrow(init: ((T) -> Unit)?): T {
    val result = borrow()
    if (init != null) {
        init(result)
    }
    return result
}
