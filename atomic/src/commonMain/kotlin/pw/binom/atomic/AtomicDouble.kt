package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicDouble(val native: InternalAtomicLong) {
    constructor(value: Double)

    fun getValue(): Double
    fun setValue(value: Double)
    fun compareAndSet(expected: Double, new: Double): Boolean
}
