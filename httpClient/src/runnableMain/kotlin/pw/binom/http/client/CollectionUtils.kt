package pw.binom.http.client

import pw.binom.collections.LinkedList
import pw.binom.collections.removeIf

internal fun <K, V> MutableMap<K, LinkedList<V>>.removeLastOrNull(key: K): V? {
  val list = this[key] ?: return null
  val value = list.removeLast()
  if (list.isEmpty()) {
    remove(key)
  }
  return value
}

internal fun <K, V> MutableMap<K, LinkedList<V>>.addFirst(key: K, value: V) {
  var list = this[key]
  if (list == null) {
    list = LinkedList()
    this[key] = list
  }
  list.addFirst(value)
}

internal fun <K, V> MutableMap<K, LinkedList<V>>.removeFromListIf(func: (K, V) -> Boolean) {
  val it = entries.iterator()
  while (it.hasNext()) {
    val valueList = it.next()
    valueList.value.removeIf { value ->
      func(valueList.key, value)
    }
    if (valueList.value.isEmpty()) {
      it.remove()
    }
  }
}
