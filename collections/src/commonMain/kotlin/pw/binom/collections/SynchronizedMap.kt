package pw.binom.collections

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.synchronize

class SynchronizedMutableMap<K, V> internal constructor(
  val map: MutableMap<K, V>,
  val lock: AtomicBoolean,
) : MutableMap<K, V> {
  constructor(map: MutableMap<K, V>) : this(map, AtomicBoolean(false))

  override val size: Int
    get() = lock.synchronize { map.size }

  override fun isEmpty() = lock.synchronize { map.isEmpty() }

  override fun containsKey(key: K) = lock.synchronize { map.containsKey(key) }

  override fun containsValue(value: V) = lock.synchronize { map.containsValue(value) }

  override fun get(key: K): V? = lock.synchronize { map[key] }
  override val keys: MutableSet<K>
    get() = SynchronizedMutableSet(map.keys, lock)
  override val values: MutableCollection<V>
    get() = SynchronizedMutableCollection(map.values, lock)
  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = SynchronizedMutableSet(map.entries, lock)

  override fun put(key: K, value: V) = lock.synchronize { map.put(key, value) }

  override fun remove(key: K): V? = lock.synchronize { map.remove(key) }

  override fun putAll(from: Map<out K, V>) {
    lock.synchronize { map.putAll(from) }
  }

  override fun clear() {
    lock.synchronize { map.clear() }
  }
}
