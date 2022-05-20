package pw.binom.atomic

@JvmInline
actual value class AtomicReference<T>(val native: InternalAtomicReference<T>) {
    actual constructor(value: T) : this(InternalAtomicReference(value))

    actual inline fun compareAndSet(expected: T, new: T): Boolean =
        native.compareAndSet(expected, new)

    actual inline fun compareAndSwap(expected: T, new: T): T =
        native.updateAndGet { operand ->
            if (operand === expected) {
                new
            } else {
                operand
            }
        }

    actual inline fun getValue(): T = native.get()

    actual inline fun setValue(value: T) {
        native.set(value)
    }
}
