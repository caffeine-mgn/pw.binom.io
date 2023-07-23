package pw.binom.atomic

@JvmInline
actual value class AtomicLong(val native: InternalAtomicLong) {
  actual constructor(value: Long) : this(InternalAtomicLong(value))

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSet(expected: Long, new: Long): Boolean =
    native.compareAndSet(expected, new)

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSwap(expected: Long, new: Long): Long =
    native.compareAndExchange(expected, new)

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun addAndGet(delta: Long): Long =
    native.addAndGet(delta)

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun increment() {
    native.incrementAndGet()
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun decrement() {
    native.decrementAndGet()
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline operator fun inc(): AtomicLong {
    native.incrementAndGet()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline operator fun dec(): AtomicLong {
    native.decrementAndGet()
    return this
  }

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): Long = native.get()

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: Long) {
    native.set(value)
  }

  override fun toString(): String = "AtomicLong(value=${getValue()})"
}
