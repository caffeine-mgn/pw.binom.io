package pw.binom.atomic

actual value class AtomicLong(val native: InternalAtomicLong) {
    actual constructor(value: Long) : this(InternalAtomicLong(value))

    actual inline fun compareAndSet(expected: Long, new: Long): Boolean =
        native.compareAndSet(expected, new)

    actual inline fun compareAndSwap(expected: Long, new: Long): Long =
        native.compareAndSwap(expected, new)

    actual inline fun addAndGet(delta: Long): Long =
        native.addAndGet(delta)

    actual inline fun increment() {
        native.increment()
    }

    actual inline fun decrement() {
        native.decrement()
    }

    actual inline operator fun inc(): AtomicLong {
        native.increment()
        return this
    }

    actual inline operator fun dec(): AtomicLong {
        native.decrement()
        return this
    }

    actual inline fun getValue(): Long = native.value

    actual inline fun setValue(value: Long) {
        native.value = value
    }
}
