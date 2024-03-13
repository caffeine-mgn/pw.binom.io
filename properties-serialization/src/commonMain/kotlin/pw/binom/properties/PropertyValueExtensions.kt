package pw.binom.properties

internal class CompositeObject(
  val first: PropertyValue.Object,
  val second: PropertyValue.Object,
) : PropertyValue.Object {
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
}
