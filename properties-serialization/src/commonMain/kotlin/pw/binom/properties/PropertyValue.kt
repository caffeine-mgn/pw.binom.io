package pw.binom.properties

sealed interface PropertyValue {
  fun getByPath(path: String): PropertyValue?

  interface Object : PropertyValue {
    companion object {
      val EMPTY =
        object : Object {
          override val names: Iterable<String>
            get() = emptyList()

          override fun get(key: String): PropertyValue? = null

          override fun contains(key: String): Boolean = false
          override fun toString() = "{}"
        }
    }

    val names: Iterable<String>
    operator fun get(key: String): PropertyValue?

    operator fun contains(key: String): Boolean

    override fun getByPath(path: String): PropertyValue? {
      if (path.isEmpty()) {
        return this
      }
      val p = path.indexOf('.')
      val itemName = if (p == -1) path else path.substring(0, p)
      val other = if (p == -1) "" else path.substring(p + 1)
      return get(itemName)?.getByPath(other)
    }

    operator fun plus(other: Object): Object = CompositeObject(first = this, second = other)
  }

  interface Value : PropertyValue {
    val content: String?

    override fun getByPath(path: String): PropertyValue? = if (path.isEmpty()) this else null
  }

  interface Enumerate : PropertyValue, Object {
    companion object {
      val EMPTY =
        object : Enumerate {
          override val size: Int
            get() = 0

          override fun get(index: Int): PropertyValue? = null
          override fun toString(): String = "[]"
        }
    }

    val size: Int
    override val names: Iterable<String>
      get() = (0 until size).map { it.toString() }
    operator fun get(index: Int): PropertyValue?

    override fun get(key: String): PropertyValue? = getByIndexPath(key)

    override fun contains(key: String): Boolean {
      if (size == 0) {
        return false
      }
      if (!key.startsWith("[") || !key.endsWith("]")) {
        return false
      }
      val numStr = key.removePrefix("[").removeSuffix("]")
      val index = numStr.toIntOrNull() ?: return false
      return index in 0 until size
    }

    private fun getByIndexPath(key: String): PropertyValue? {
      if (!key.startsWith("[") || !key.endsWith("]")) {
        return null
      }
      val numStr = key.removePrefix("[").removeSuffix("]")
      val index = numStr.toIntOrNull() ?: return null
      return get(index)
    }

    override fun getByPath(path: String): PropertyValue? {
      if (path.isEmpty()) {
        return this
      }
      val p = path.indexOf('.')
      return if (p == -1) {
        getByIndexPath(path)
      } else {
        getByIndexPath(path.substring(0, p))?.getByPath(path.substring(p + 1))
      }
    }

    operator fun plus(other: Enumerate): Enumerate = CompositeEnumerate(first = this, second = other)
  }
}
