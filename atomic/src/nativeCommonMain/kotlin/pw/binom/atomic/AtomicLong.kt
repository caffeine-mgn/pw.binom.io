package pw.binom.atomic

actual value class AtomicLong(val native: InternalAtomicLong) {
  actual constructor(value: Long) : this(InternalAtomicLong(value))

  actual inline fun compareAndSet(expected: Long, new: Long): Boolean =
    native.compareAndSet(expected, new)

  actual inline fun compareAndSwap(expected: Long, new: Long): Long =
    native.compareAndExchange(expected, new)

  actual inline fun addAndGet(delta: Long): Long =
    native.addAndGet(delta)

  actual inline fun increment() {
    native.incrementAndGet()
  }

  actual inline fun decrement() {
    native.decrementAndGet()
  }

  actual inline operator fun inc(): AtomicLong {
    native.incrementAndGet()
    return this
  }

  actual inline operator fun dec(): AtomicLong {
    native.decrementAndGet()
    return this
  }

  actual inline fun getValue(): Long = native.value

  actual inline fun setValue(value: Long) {
    native.value = value
  }

  override fun toString(): String = "AtomicLong(value=${getValue()})"
}
