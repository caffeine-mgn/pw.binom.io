package pw.binom.pool

open class DefaultPool<T : Any>(val capacity: Int, val new: () -> T) : ObjectPool<T> {

    protected val pool = arrayOfNulls<Any>(capacity)
    var size = 0
        protected set

    override fun borrow(init: ((T) -> Unit)?): T {
        if (size == 0)
            return new().also { init?.invoke(it) }
        val index = --size
        val result = pool[index]!!
        pool[index] = null
        init?.invoke(result as T)
        return result as T
    }

    override fun recycle(value: T) {
        if (size < capacity) {
            pool[size++] = value
        }
    }

}