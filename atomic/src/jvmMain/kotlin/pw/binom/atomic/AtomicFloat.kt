package pw.binom.atomic

@JvmInline
actual value class AtomicFloat(val native: InternalAtomicInt) {
  actual constructor(value: Float) : this(InternalAtomicInt(value.toBits()))

  actual fun compareAndSet(expected: Float, new: Float): Boolean {
    if (native.get() != expected.toBits()) {
      return false
    }
    native.set(new.toBits())
    return true
  }

  actual inline fun getValue(): Float = Float.fromBits(native.get())

  actual inline fun setValue(value: Float) {
    native.set(value.toBits())
  }
}
