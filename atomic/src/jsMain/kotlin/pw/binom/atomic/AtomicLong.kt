package pw.binom.atomic

actual value class AtomicLong(val native: InternalAtomicLong) {
    actual constructor(value: Long) : this(InternalAtomicLong(value))

    actual inline fun compareAndSet(expected: Long, new: Long): Boolean {
        if (native.value != expected) {
            return false
        }
        native.value = new
        return true
    }

    actual inline fun compareAndSwap(expected: Long, new: Long): Long {
        val oldValue = native.value
        if (oldValue != expected) {
            return native.value
        }
        native.value = new
        return oldValue
    }

    actual inline fun addAndGet(delta: Long): Long {
        native.value += delta
        return native.value
    }

    actual inline fun increment() {
        addAndGet(1)
    }

    actual inline fun decrement() {
        addAndGet(-1)
    }

    actual inline operator fun inc(): AtomicLong {
        increment()
        return this
    }

    actual inline operator fun dec(): AtomicLong {
        decrement()
        return this
    }

    actual inline fun getValue(): Long = native.value

    actual inline fun setValue(value: Long) {
        native.value = value
    }
}
