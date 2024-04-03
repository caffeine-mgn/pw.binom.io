package pw.binom.properties

internal class CompositeObject(
  val first: PropertyValue.Object,
  val second: PropertyValue.Object,
) : PropertyValue.Object {
  override val names: Iterable<String>
    get() {
      val result = HashSet<String>()
      result += first.names
      result += second.names
      return result
    }

  override fun get(key: String): PropertyValue? =
    when {
      second.contains(key) && first.contains(key) -> {
        val firstValue = first[key]
        val secondValue = second[key]
        when {
          firstValue is PropertyValue.Enumerate && secondValue is PropertyValue.Enumerate -> firstValue + secondValue
          firstValue is PropertyValue.Object && secondValue is PropertyValue.Object -> firstValue + secondValue
          else -> secondValue
        }
      }

      second.contains(key) -> second[key]
      first.contains(key) -> first[key]
      else -> null
    }

  override fun contains(key: String): Boolean = first.contains(key) || second.contains(key)

  override fun toString(): String {
    val result = HashMap<String, PropertyValue?>()
    names.forEach { name ->
      result[name] = get(name)
    }
    return "{" + result.entries.joinToString(separator = ", ", transform = { "${it.key}: ${it.value}" })
  }
}

inline fun PropertyValue.Enumerate.forEach(func: (PropertyValue?) -> Unit) {
  for (i in 0 until size) {
    func(get(i))
  }
}

internal class CompositeEnumerate(val first: PropertyValue.Enumerate, val second: PropertyValue.Enumerate) :
  PropertyValue.Enumerate {
  override val size: Int
    get() = first.size + second.size

  override fun get(index: Int): PropertyValue? =
    when {
      index in 0 until first.size -> first[index]
      index - first.size in 0 until second.size -> first[index - first.size]
      else -> null
    }

  override fun toString(): String {
    val sb = StringBuilder("[")
    var isFirst = true
    first.forEach { value ->
      if (!isFirst) {
        sb.append(", ")
      }
      isFirst = false
      sb.append(value)
    }
    second.forEach { value ->
      if (!isFirst) {
        sb.append(", ")
      }
      isFirst = false
      sb.append(value)
    }
    sb.append("]")
    return sb.toString()
  }
}
