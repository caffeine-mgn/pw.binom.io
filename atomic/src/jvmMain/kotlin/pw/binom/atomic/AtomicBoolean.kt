package pw.binom.atomic

@JvmInline
actual value class AtomicBoolean actual constructor(val native: InternalAtomicBoolean) {
    actual constructor(value: Boolean) : this(InternalAtomicBoolean(value))

    actual inline fun compareAndSet(expected: Boolean, new: Boolean): Boolean =
        native.compareAndSet(expected, new)

    actual inline fun compareAndSwap(expected: Boolean, new: Boolean): Boolean =
        native.compareAndSet(expected, new)

    actual inline fun getValue(): Boolean = native.get()

    actual inline fun setValue(value: Boolean) {
        native.set(value)
    }

    override fun toString(): String = "AtomicBoolean(value=${getValue()})"
}
