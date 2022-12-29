package pw.binom.pool

class FixedSizePool<T : Any>(capacity: Int, val manager: ObjectFactory<T>) : AbstractFixedSizePool<T>(capacity) {
    override fun new(): T = manager.allocate(this)

    override fun reset(value: T) {
        super.reset(value)
        manager.reset(value, this)
    }

    override fun borrow(): T {
        val obj = super.borrow()
        manager.prepare(obj, this)
        return obj
    }

    override fun free(value: T) {
        manager.deallocate(value, this)
    }
}
