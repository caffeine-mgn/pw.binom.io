package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicLong(val native: InternalAtomicLong) {
    constructor(value: Long)

    inline fun compareAndSet(expected: Long, new: Long): Boolean
    inline fun compareAndSwap(expected: Long, new: Long): Long
    inline fun addAndGet(delta: Long): Long
    inline fun increment()
    inline fun decrement()
    inline operator fun inc(): AtomicLong
    inline operator fun dec(): AtomicLong
    inline fun getValue(): Long
    inline fun setValue(value: Long)
}
