package pw.binom.atomic

actual value class AtomicLong(val native: InternalAtomicLong) {
  actual constructor(value: Long) : this(InternalAtomicLong(value))

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSet(expected: Long, new: Long): Boolean {
    if (native.value != expected) {
      return false
    }
    native.value = new
    return true
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSwap(expected: Long, new: Long): Long {
    val oldValue = native.value
    if (oldValue != expected) {
      return native.value
    }
    native.value = new
    return oldValue
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun addAndGet(delta: Long): Long {
    native.value += delta
    return native.value
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun increment() {
    addAndGet(1)
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun decrement() {
    addAndGet(-1)
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline operator fun inc(): AtomicLong {
    increment()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline operator fun dec(): AtomicLong {
    decrement()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): Long = native.value

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: Long) {
    native.value = value
  }

  override fun toString(): String = "AtomicLong(value=${getValue()})"
}
