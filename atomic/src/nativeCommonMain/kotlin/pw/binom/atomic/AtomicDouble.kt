package pw.binom.atomic

actual value class AtomicDouble(val native: InternalAtomicLong) {
    actual constructor(value: Double) : this(InternalAtomicLong(value.toBits()))

    actual fun compareAndSet(expected: Double, new: Double): Boolean {
        if (native.value != expected.toBits()) {
            return false
        }
        native.value = new.toBits()
        return true
    }

    actual inline fun getValue(): Double = Double.fromBits(native.value)

    actual inline fun setValue(value: Double) {
        native.value = value.toBits()
    }
}
