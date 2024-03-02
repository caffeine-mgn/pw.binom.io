package pw.binom.mq

import pw.binom.collections.emptyIterator

interface Headers : Iterable<Pair<String, String>> {
  companion object {
    val empty =
      object : Headers {
        override fun get(key: String): List<String>? = null

        override fun clone(): Headers = this

        override val size: Int
          get() = 0

        override fun iterator(): Iterator<Pair<String, String>> = emptyIterator()
      }
  }

  operator fun get(key: String): List<String>?

  fun getFirst(key: String) = get(key)?.firstOrNull()

  fun getLast(key: String) = get(key)?.lastOrNull()

  fun clone(): Headers

  val size: Int
  val isEmpty: Boolean
    get() = size == 0
  val isNotEmpty
    get() = !isEmpty

  fun filter(func: (key: String, value: String) -> Boolean): Headers {
    val map = HashMap<String, ArrayList<String>>()
    forEach { (key, value) ->
      if (func(key, value)) {
        map.getOrPut(key) { ArrayList() }.add(value)
      }
    }
    return if (map.isEmpty()) {
      empty
    } else {
      map.forEach {
        it.value.trimToSize()
      }
      MapHeaders(map)
    }
  }
}
