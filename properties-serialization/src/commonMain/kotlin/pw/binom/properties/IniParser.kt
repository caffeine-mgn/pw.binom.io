package pw.binom.properties

object IniParser {
  private data class Obj(val map: Map<String, PropertyValue?>) : PropertyValue.Object {
    override fun get(key: String): PropertyValue? = map[key]

    override fun contains(key: String): Boolean = map.containsKey(key)
  }

  private data class EList(val content: List<PropertyValue?>) : PropertyValue.Enumerate {
    override val size: Int
      get() = content.size

    override fun get(index: Int): PropertyValue? = content[index]
  }

  private data class Val(override val content: String?) : PropertyValue.Value

  fun parseLines(lines: List<String>) = convert(parseLinesInternal(lines.iterator())) as PropertyValue.Object

  fun parseLines(lines: Array<String>) = convert(parseLinesInternal(lines.iterator())) as PropertyValue.Object

  fun parseMap(lines: Map<String, String?>) = convert(parseValuesInternal(lines)) as PropertyValue.Object

  private fun convert(obj: Any?): PropertyValue? =
    when (obj) {
      is Map<*, *> -> Obj(obj.asSequence().map { it.key as String to convert(it.value) }.toMap())
      is List<*> -> EList(obj.map { convert(it) })
      is String -> Val(obj)
      null -> null
      else -> TODO()
    }

  private fun parseValuesInternal(lines: Map<String, String?>): Map<String, Any?> {
    val root = HashMap<String, Any?>()

    lines.forEach { line ->
      val path = line.key
      val value = line.value
      parseLine(dest = root, path = path, value = value)
    }
    return root
  }

  private fun parseLinesInternal(lines: Iterator<String>): Map<String, Any?> {
    val root = HashMap<String, Any?>()

    lines.forEach { line ->
      val items = line.split('=', limit = 2)
      val path = items[0]
      val value = items.getOrNull(1)
      parseLine(dest = root, path = path, value = value)
    }
    return root
  }

  private fun parseLine(
    dest: HashMap<String, Any?>,
    path: String,
    value: String?,
  ) {
//    val items = line.split('=', limit = 2)
//    val path = items[0]
//    val value = items.getOrNull(1)

    val pathElements = path.split('.')
    var localNode = dest
    pathElements.forEachIndexed { index, field ->
      val isLast = pathElements.lastIndex == index
      if (field.endsWith("]")) {
        val indexOfChar = field.lastIndexOf('[')
        if (indexOfChar == -1) {
          TODO()
        }
        val listIndexStr = field.substring(indexOfChar + 1, field.lastIndex)
        val listIndex = listIndexStr.toIntOrNull() ?: TODO("Can't parse $listIndexStr to Int")
        val fieldName = field.substring(0, indexOfChar)
        var list = localNode[fieldName]
        if (list == null) {
          list = ArrayList<Any?>()
          localNode[fieldName] = list
        } else {
          if (list !is ArrayList<*>) {
            TODO()
          }
        }
        list as ArrayList<Any?>
        while (list.lastIndex < listIndex) {
          list.add(null)
        }
        if (isLast) {
          list[listIndex] = value
        } else {
          localNode = HashMap()
          list[listIndex] = localNode
        }
        return@forEachIndexed
      }

      if (isLast) {
        localNode[field] = value
      } else {
        var k = localNode[field]
        if (k == null) {
          k = HashMap<String, Any?>()
          localNode[field] = k
        } else {
          if (k !is HashMap<*, *>) {
            TODO()
          }
        }
        localNode = k as HashMap<String, Any?>
      }
    }
  }
}
