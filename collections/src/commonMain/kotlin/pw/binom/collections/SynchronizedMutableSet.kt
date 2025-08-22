package pw.binom.collections

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize

class SynchronizedMutableSet<K> internal constructor(
  private val set: MutableSet<K>, private val lock: AtomicBoolean,
) : MutableSet<K> {
  constructor(set: MutableSet<K>) : this(set, AtomicBoolean(false))

  override val size: Int
    get() = lock.synchronize { set.size }

  override fun iterator(): MutableIterator<K> = SynchronizedMutableIterator(set.iterator(), lock)

  override fun add(element: K) = lock.synchronize { set.add(element) }

  override fun remove(element: K) = lock.synchronize { set.remove(element) }

  override fun addAll(elements: Collection<K>) = lock.synchronize { set.addAll(elements) }

  override fun removeAll(elements: Collection<K>) = lock.synchronize { set.removeAll(elements) }

  override fun retainAll(elements: Collection<K>) = lock.synchronize { set.retainAll(elements) }

  override fun clear() {
    lock.synchronize { set.clear() }
  }

  override fun isEmpty() = lock.synchronize { set.isEmpty() }

  override fun contains(element: K) = lock.synchronize { set.contains(element) }

  override fun containsAll(elements: Collection<K>) = lock.synchronize { set.containsAll(elements) }
}
