package pw.binom.pool

import pw.binom.io.Closeable

/**
 * Object Pool. [borrow] return object from pool. When object is not need you should call [recycle] for free object
 */
interface ObjectPool<T : Any> : Closeable {
  fun borrow(owner: Any? = null): T

  /**
   * Return object to pool
   */
  fun recycle(value: T)
}

inline fun <T : Any, R> ObjectPool<T>.tryBorrow(owner: Any? = null, func: (T) -> R): R {
  val r = borrow(owner = owner)
  return try {
    func(r)
  } catch (e: Throwable) {
    recycle(r)
    throw e
  }
}
