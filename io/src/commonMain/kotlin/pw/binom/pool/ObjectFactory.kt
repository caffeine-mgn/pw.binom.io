package pw.binom.pool

interface ObjectFactory<T : Any> {
    fun allocate(pool: ObjectPool<T>): T
    fun deallocate(value: T, pool: ObjectPool<T>)
    fun prepare(value: T, pool: ObjectPool<T>) {
        // Do nothing
    }

    fun reset(value: T, pool: ObjectPool<T>) {
        // Do nothing
    }
}
