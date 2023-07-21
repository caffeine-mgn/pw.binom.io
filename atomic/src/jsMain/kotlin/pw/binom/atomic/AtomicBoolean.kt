package pw.binom.atomic

actual value class AtomicBoolean(val native: InternalAtomicBoolean) {
  actual constructor(value: Boolean) : this(InternalAtomicBoolean(value))

  actual inline fun compareAndSet(expected: Boolean, new: Boolean): Boolean {
    if (native.value != expected) {
      return false
    }
    native.value = new
    return true
  }

  actual inline fun compareAndSwap(expected: Boolean, new: Boolean): Boolean {
    val old = native.value
    if (native.value != expected) {
      return old
    }
    native.value = new
    return expected
  }

  actual inline fun getValue(): Boolean = native.value

  actual inline fun setValue(value: Boolean) {
    native.value = value
  }

  override fun toString(): String = "AtomicBoolean(value=${getValue()})"
}
