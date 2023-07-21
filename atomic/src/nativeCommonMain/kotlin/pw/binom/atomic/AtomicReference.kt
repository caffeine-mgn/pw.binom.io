package pw.binom.atomic

actual value class AtomicReference<T>(val native: InternalAtomicReference<T>) {
  actual constructor(value: T) : this(InternalAtomicReference(value))

  actual inline fun compareAndSet(expected: T, new: T): Boolean =
    native.compareAndSet(expected, new)

  actual inline fun compareAndSwap(expected: T, new: T): T =
    native.compareAndSwap(expected, new)

  actual inline fun getValue(): T = native.value

  actual inline fun setValue(value: T) {
    native.value = value
  }
}
