package pw.binom.pool

/**
 * Object pool. All methods are thread-save
 */
open class DefaultPool<T : Any>(capacity: Int, new: (DefaultPool<T>) -> T) : AbstractFixedSizePool<T>(capacity) {

    private val newFunc: (DefaultPool<T>) -> T = new

    override fun new(): T = newFunc(this)
}
