package pw.binom.mq

import pw.binom.collections.emptyIterator

open class MapHeaders(val map: Map<String, List<String>>) : Headers {
  private class MapIterator(map: Map<String, List<String>>) : Iterator<Pair<String, String>> {
    private val mapIterator = map.iterator()
    private var listIterator: Iterator<String> = emptyIterator()

    override fun hasNext(): Boolean = listIterator.hasNext() || mapIterator.hasNext()

    private var currentKey = ""

    override fun next(): Pair<String, String> {
      if (listIterator.hasNext()) {
        return currentKey to listIterator.next()
      }
      if (!mapIterator.hasNext()) {
        throw NoSuchElementException()
      }
      val e = mapIterator.next()
      currentKey = e.key
      listIterator = e.value.iterator()
      return currentKey to listIterator.next()
    }
  }

  override fun get(key: String): List<String>? = map[key]

  override fun clone(): Headers = MapHeaders(map)

  override val size: Int
    get() =
      run {
        var sum = 0
        map.forEach {
          sum += it.value.size
        }
        sum
      }

  override fun iterator(): Iterator<Pair<String, String>> = MapIterator(map)

  override fun toString(): String {
    val sb = StringBuilder()
    sb.append("Headers(")
    var first = true
    map.forEach { (key, values) ->
      values.forEach { value ->
        if (!first) {
          sb.append(", ")
        } else {
          first = false
        }
        sb.append("$key=$value")
      }
    }
    sb.append(")")
    return sb.toString()
  }
}
