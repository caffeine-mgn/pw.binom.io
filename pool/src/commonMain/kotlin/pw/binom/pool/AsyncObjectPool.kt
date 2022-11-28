package pw.binom.pool

interface AsyncObjectPool<T : Any> {
    suspend fun borrow(): T
    suspend fun recycle(value: T)
}
