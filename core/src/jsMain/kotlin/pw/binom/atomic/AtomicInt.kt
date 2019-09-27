package pw.binom.atomic

actual class AtomicInt actual constructor(actual var value: Int) {
    actual fun compareAndSet(expected: Int, new: Int): Boolean {
        if (value == expected) {
            value = new
            return true
        }
        return false
    }

    actual fun compareAndSwap(expected: Int, new: Int): Int {
        if (value == expected) {
            val old = value
            value = new
            return old
        }
        return new
    }

    actual fun addAndGet(delta: Int): Int {
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