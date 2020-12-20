package pw.binom.pool

interface ObjectPool<T:Any> {
    fun borrow(init:((T)->Unit)?=null): T
    fun recycle(value: T)
}