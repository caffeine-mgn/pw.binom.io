package pw.binom.atomic

actual class AtomicReference<T> actual constructor(actual var value: T) {
    actual fun compareAndSet(expected: T, new: T): Boolean {
        if (value===expected){
            value=new
            return true
        }
        return false
    }

    actual fun compareAndSwap(expected: T, new: T): T {
        if (value === expected) {
            val old = value
            value = new
            return old
        }
        return new
    }
}