package pw.binom.collections

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize

class SynchronizedMutableIterator<T> internal constructor(
  private val iterator: MutableIterator<T>,
  private val lock: AtomicBoolean,
) : MutableIterator<T> {
  constructor(iterator: MutableIterator<T>) : this(iterator, AtomicBoolean(false))

  override fun remove() = lock.synchronize { iterator.remove() }

  override fun next(): T = lock.synchronize { iterator.next() }

  override fun hasNext() = lock.synchronize { iterator.hasNext() }
}
