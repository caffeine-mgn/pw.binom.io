package pw.binom.atomic

@JvmInline
actual value class AtomicReference<T>(val native: InternalAtomicReference<T>) {
  actual constructor(value: T) : this(InternalAtomicReference(value))

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSet(expected: T, new: T): Boolean =
    native.compareAndSet(expected, new)

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun compareAndSwap(expected: T, new: T): T =
    native.compareAndExchange(expected, new)

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun getValue(): T = native.get()

  @Suppress("NOTHING_TO_INLINE")
  actual inline fun setValue(value: T) {
    native.set(value)
  }

  override fun toString(): String = "AtomicReference(value=${getValue()})"
}
