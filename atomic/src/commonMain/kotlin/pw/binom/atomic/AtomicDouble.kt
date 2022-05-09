package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicFloat(val native: InternalAtomicInt) {
    constructor(value: Float)

    fun getValue(): Float
    fun setValue(value: Float)
    fun compareAndSet(expected: Float, new: Float): Boolean
}
