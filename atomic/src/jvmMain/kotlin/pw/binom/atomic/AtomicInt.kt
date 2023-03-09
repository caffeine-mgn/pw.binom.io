package pw.binom.atomic

@JvmInline
actual value class AtomicInt(@PublishedApi internal val native: InternalAtomicInt) {
    actual constructor(value: Int) : this(InternalAtomicInt(value))

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun compareAndSet(expected: Int, new: Int): Boolean =
        native.compareAndSet(expected, new)

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun compareAndSwap(expected: Int, new: Int): Int =
        native.compareAndExchange(expected, new)

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun addAndGet(delta: Int): Int =
        native.addAndGet(delta)

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun increment() {
        native.incrementAndGet()
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun decrement() {
        native.decrementAndGet()
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline operator fun inc(): AtomicInt {
        native.incrementAndGet()
        return this
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline operator fun dec(): AtomicInt {
        native.decrementAndGet()
        return this
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun getValue(): Int = native.get()

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun setValue(value: Int) {
        native.set(value)
    }
}
