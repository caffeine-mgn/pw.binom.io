package pw.binom.pool

class FixedSizePool<T : Any>(capacity: Int, val manager: PoolObjectFactory<T>) : AbstractFixedSizePool<T>(capacity) {
    override fun new(): T = manager.new(this)

    override fun free(value: T) {
        manager.free(value)
    }
}
