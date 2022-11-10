package pw.binom.pool

import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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

interface ObjectFactory<T : Any> {
    fun allocate(pool: ObjectPool<T>): T
    fun deallocate(value: T, pool: ObjectPool<T>)
    fun reset(value: T, pool: ObjectPool<T>) {
        // Do nothing
    }
}

/**
 * Return object from pool and apply [init] for config obejct from pool
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> ObjectPool<T>.borrow(init: ((T) -> Unit)): T {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        returnsNotNull()
    }
    val result = borrow()
    init(result)
    return result
}

@OptIn(ExperimentalContracts::class)
inline fun <T : Any, R> ObjectPool<T>.using(action: ((T) -> R)): R {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    val value = borrow()
    try {
        return action(value)
    } finally {
        recycle(value)
    }
}
