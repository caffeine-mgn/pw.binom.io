package pw.binom.pool

open class DefaultPool<T : Any>(val capacity: Int, val new: () -> T) : ObjectPool<T> {

    private val pool = arrayOfNulls<Any>(capacity) as Array<T?>
    var size = 0
        private set

    override fun borrow(init: ((T) -> Unit)?): T {
        if (size == 0)
            return new().also { init?.invoke(it) }
        val index = --size
        val result = pool[index]!!
        pool[index] = null
        init?.invoke(result)
        return result
    }

    override fun recycle(value: T) {
        if (size < capacity) {
            pool[size++] = value
        }
    }

}