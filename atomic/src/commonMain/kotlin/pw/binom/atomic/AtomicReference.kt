package pw.binom.atomic

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect value class AtomicReference<T>(val native: InternalAtomicReference<T>) {
    constructor(value: T)

    inline fun compareAndSet(expected: T, new: T): Boolean
    inline fun compareAndSwap(expected: T, new: T): T
    inline fun getValue(): T
    inline fun setValue(value: T)
}
