package pw.binom.pool

open class AsyncDefaultPool<T : Any>(val capacity: Int, val new: suspend () -> T) : AsyncObjectPool<T> {

    protected val pool = arrayOfNulls<Any>(capacity)
    var size = 0
        protected set

    @Suppress("UNCHECKED_CAST")
    override suspend fun borrow(): T {
        if (size == 0) {
            return new()
        }
        val index = --size
        val result = pool[index]!!
        pool[index] = null
        return result as T
    }

    override suspend fun recycle(value: T) {
        if (size < capacity) {
            pool[size++] = value
        }
    }
}
