package pw.binom.collections

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize

class SynchronizedMutableCollection<T> internal constructor(
  private val collection: MutableCollection<T>,
  private val lock: AtomicBoolean,
) : MutableCollection<T> {
  constructor(collection: MutableCollection<T>) : this(collection, AtomicBoolean(false))

  override fun iterator() = SynchronizedMutableIterator(collection.iterator(), lock)

  override fun add(element: T) = lock.synchronize { collection.add(element) }

  override fun remove(element: T) = lock.synchronize { collection.remove(element) }

  override fun addAll(elements: Collection<T>) = lock.synchronize { collection.addAll(elements) }

  override fun removeAll(elements: Collection<T>) = lock.synchronize { collection.removeAll(elements) }

  override fun retainAll(elements: Collection<T>) = lock.synchronize { collection.retainAll(elements) }

  override fun clear() = lock.synchronize { collection.clear() }

  override val size: Int
    get() = lock.synchronize { collection.size }

  override fun isEmpty() = lock.synchronize { collection.isEmpty() }

  override fun contains(element: T) = lock.synchronize { collection.contains(element) }

  override fun containsAll(elements: Collection<T>) = lock.synchronize { collection.containsAll(elements) }
}
