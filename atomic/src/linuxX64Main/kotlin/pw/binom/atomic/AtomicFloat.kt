package pw.binom.atomic

actual value class AtomicFloat(val native: InternalAtomicInt) {
    actual constructor(value: Float) : this(InternalAtomicInt(value.toBits()))

    actual fun compareAndSet(expected: Float, new: Float): Boolean {
        if (native.value != expected.toBits()) {
            return false
        }
        native.value = new.toBits()
        return true
    }

    actual inline fun getValue(): Float = Float.fromBits(native.value)

    actual inline fun setValue(value: Float) {
        native.value = value.toBits()
    }
}
