package pw.binom.atomic

@JvmInline
actual value class AtomicDouble(val native: InternalAtomicLong) {
  actual constructor(value: Double) : this(InternalAtomicLong(value.toBits()))

  actual fun compareAndSet(expected: Double, new: Double): Boolean {
    if (native.get() != expected.toBits()) {
      return false
    }
    native.set(new.toBits())
    return true
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): Double = Double.fromBits(native.get())

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: Double) {
    native.set(value.toBits())
  }
  override fun toString(): String = "AtomicDouble(value=${getValue()})"
}
