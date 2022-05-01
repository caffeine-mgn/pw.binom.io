package pw.binom.pool

interface AsyncObjectPool<T : Any> {
    suspend fun borrow(init: ((T) -> Unit)? = null): T
    suspend fun recycle(value: T)
}
