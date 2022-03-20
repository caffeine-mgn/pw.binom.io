package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicBoolean(val native: InternalAtomicBoolean) {
    constructor(value: Boolean)

    inline fun compareAndSet(expected: Boolean, new: Boolean): Boolean
    inline fun compareAndSwap(expected: Boolean, new: Boolean): Boolean

    inline fun getValue(): Boolean
    inline fun setValue(value: Boolean)
}
