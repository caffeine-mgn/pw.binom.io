package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicInt(val native: InternalAtomicInt) {
    constructor(value: Int)
    inline fun compareAndSet(expected: Int, new: Int): Boolean
    inline fun compareAndSwap(expected: Int, new: Int): Int
    inline fun addAndGet(delta: Int): Int
    inline fun increment()
    inline fun decrement()
    inline operator fun inc(): AtomicInt
    inline operator fun dec(): AtomicInt
    inline fun getValue(): Int
    inline fun setValue(value: Int)
}
