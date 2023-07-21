package pw.binom.collections

import kotlin.jvm.JvmInline

@JvmInline
value class MultiValueMap<K, V>(val map: MutableMap<K, MutableCollection<V>>) {
  constructor() : this(HashMap())

  fun add(key: K, value: V) {
    map.getOrPut(key) { ArrayList() }.add(value)
  }

  operator fun get(key: K) = map[key]
  operator fun set(key: K, value: MutableCollection<V>) {
    map[key] = value
  }

  inline fun asSequence() = map.asSequence()

  fun asValueSequence() = map.asSequence().flatMap { pair -> pair.value.asSequence().map { pair.key to it } }
  inline fun forEach(func: (Map.Entry<K, Collection<V>>) -> Unit) {
    map.forEach(func)
  }

  inline fun valueForEach(func: (Pair<K, V>) -> Unit) {
    asValueSequence().forEach(func)
  }
}
