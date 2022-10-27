package pw.binom.pool

class FixedSizePool<T : Any>(capacity: Int, val manager: ObjectFactory<T>) : AbstractFixedSizePool<T>(capacity) {
    override fun new(): T = manager.allocate(this)

    override fun free(value: T) {
        manager.deallocate(value, this)
    }
}
