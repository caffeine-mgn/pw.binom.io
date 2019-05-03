package pw.binom.atomic

expect class AtomicLong(value: Long) {
    fun compareAndSet(expected: Long, new: Long): Boolean
    fun compareAndSwap(expected: Long, new: Long): Long
    fun addAndGet(delta: Long): Long
    fun increment()
    fun decrement()

    var value:Long
}