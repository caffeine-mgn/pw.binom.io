package pw.binom.atomic

actual value class AtomicInt(@PublishedApi internal val native: InternalAtomicInt) {
  actual constructor(value: Int) : this(InternalAtomicInt(value))

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSet(expected: Int, new: Int): Boolean {
    if (native.value != expected) {
      return false
    }
    native.value = new
    return true
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSwap(expected: Int, new: Int): Int {
    val oldValue = native.value
    if (oldValue != expected) {
      return native.value
    }
    native.value = new
    return oldValue
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun addAndGet(delta: Int): Int {
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
  actual inline operator fun inc(): AtomicInt {
    increment()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline operator fun dec(): AtomicInt {
    decrement()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): Int = native.value

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: Int) {
    native.value = value
  }
}
