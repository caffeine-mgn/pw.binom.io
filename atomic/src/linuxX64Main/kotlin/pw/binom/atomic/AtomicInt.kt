package pw.binom.atomic

actual value class AtomicInt(@PublishedApi internal val native: InternalAtomicInt) {
    actual constructor(value: Int) : this(InternalAtomicInt(value))

    actual inline fun compareAndSet(expected: Int, new: Int): Boolean =
        native.compareAndSet(expected, new)

    actual inline fun compareAndSwap(expected: Int, new: Int): Int =
        native.compareAndSwap(expected, new)

    actual inline fun addAndGet(delta: Int): Int =
        native.addAndGet(delta)

    actual inline fun increment() {
        native.increment()
    }

    actual inline fun decrement() {
        native.decrement()
    }

    actual inline operator fun inc(): AtomicInt {
        native.increment()
        return this
    }

    actual inline operator fun dec(): AtomicInt {
        native.decrement()
        return this
    }

    actual inline fun getValue(): Int = native.value

    actual inline fun setValue(value: Int) {
        native.value = value
    }
}
