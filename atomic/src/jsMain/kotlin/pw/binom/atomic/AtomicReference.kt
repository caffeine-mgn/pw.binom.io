package pw.binom.atomic

actual value class AtomicReference<T>(val native: InternalAtomicReference<T>) {
  actual constructor(value: T) : this(InternalAtomicReference(value))

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSet(expected: T, new: T): Boolean {
    if (native.value !== expected) {
      return false
    }
    native.value = new
    return true
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSwap(expected: T, new: T): T {
    val oldValue = native.value
    if (oldValue !== expected) {
      return native.value
    }
    native.value = new
    return oldValue
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): T = native.value

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: T) {
    native.value = value
  }

  override fun toString(): String = "AtomicReference(value=${getValue()})"
}
