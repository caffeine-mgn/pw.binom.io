package pw.binom.atomic

actual value class AtomicInt(@PublishedApi internal val native: InternalAtomicInt) {
  actual constructor(value: Int) : this(InternalAtomicInt(value))

  actual inline fun compareAndSet(expected: Int, new: Int): Boolean =
    native.compareAndSet(expected, new)

  actual inline fun compareAndSwap(expected: Int, new: Int): Int =
    native.compareAndExchange(expected, new)

  actual inline fun addAndGet(delta: Int): Int =
    native.addAndGet(delta)

  actual inline fun increment() {
    native.incrementAndGet()
  }

  actual inline fun decrement() {
    native.decrementAndGet()
  }

  actual inline operator fun inc(): AtomicInt {
    native.incrementAndGet()
    return this
  }

  actual inline operator fun dec(): AtomicInt {
    native.decrementAndGet()
    return this
  }

  actual inline fun getValue(): Int = native.value

  actual inline fun setValue(value: Int) {
    native.value = value
  }

  override fun toString(): String = "AtomicInt(value=${getValue()})"
}
