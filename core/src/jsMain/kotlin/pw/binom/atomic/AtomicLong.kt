package pw.binom.atomic

actual class AtomicLong actual constructor(actual var value: Long) {

    actual fun compareAndSet(expected: Long, new: Long): Boolean {
        if (value == expected) {
            value = new
            return true
        }
        return false
    }

    actual fun compareAndSwap(expected: Long, new: Long): Long {
        if (value == expected) {
            val old = value
            value = new
            return old
        }
        return new
    }

    actual fun addAndGet(delta: Long): Long {
        value + delta
        return value
    }

    actual fun increment() {
        value++
    }

    actual fun decrement() {
        value--
    }
}