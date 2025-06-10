package pw.binom.xml.dom

class NameSpaceHolder(val root: XElement.Tag) {
  companion object {
    private const val DEFAULT_NAMESPACE = "xmlns"
    private const val NAMESPACE_ATTR_PREFIX = "$DEFAULT_NAMESPACE:"

    fun findNameSpaceByPrefix(prefix: String, element: XElement.Tag): String? {
      var current: XElement.Tag? = element
      while (current != null) {
        val foundUrl = current.attributes.entries.find { (key, _) ->
          if (key.startsWith(NAMESPACE_ATTR_PREFIX)) {
            key.substring(NAMESPACE_ATTR_PREFIX.length + 1) == prefix
          } else {
            false
          }
        }?.value
        if (foundUrl != null) {
          return foundUrl
        }
        current = current.parent
      }
      return null
    }
  }

  fun setNameSpace(nameSpace: String, element: XElement.Tag) {
    val existDefaultNs = element.attributes[DEFAULT_NAMESPACE]
    if (existDefaultNs != null) {
      if (existDefaultNs == nameSpace) {
        return
      } else {
        element.attributes.remove(DEFAULT_NAMESPACE)
      }
    }
  }

  fun getNameSpace(element: XElement.Tag): String? {
    val nameSpaceSeparator = element.name.indexOf(':')
    return if (nameSpaceSeparator > 0) {
      val nameSpacePrefix = element.name.substring(0, nameSpaceSeparator)
      return findNameSpaceByPrefix(
        prefix = nameSpacePrefix,
        element = element
      ) ?: throw IllegalStateException("Can't find namespace by prefix \"$nameSpacePrefix\"")
    } else {
      findDefaultNameSpace(element)
    }
  }

  private fun findDefaultNameSpace(element: XElement.Tag): String? {
    var current: XElement.Tag? = element
    while (current != null) {
      val ns = current.attributes[DEFAULT_NAMESPACE]
      if (ns != null) {
        return ns
      }
      current = current.parent
    }
    return null
  }


}
