package pw.binom.atomic

actual value class AtomicBoolean(val native: InternalAtomicBoolean) {
  actual constructor(value: Boolean) : this(InternalAtomicBoolean(if (value) 1 else 0))

  actual inline fun compareAndSet(expected: Boolean, new: Boolean): Boolean =
    native.compareAndSet(if (expected) 1 else 0, if (new) 1 else 0)

  actual inline fun compareAndSwap(expected: Boolean, new: Boolean): Boolean =
    native.compareAndSwap(if (expected) 1 else 0, if (new) 1 else 0) > 0

  actual inline fun getValue(): Boolean = native.value > 0

  actual inline fun setValue(value: Boolean) {
    native.value = if (value) 1 else 0
  }

  override fun toString(): String = "AtomicBoolean(value=${getValue()})"
}
