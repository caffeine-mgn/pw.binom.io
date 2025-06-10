package pw.binom.xml.serialization

import kotlinx.serialization.SerializationException
import pw.binom.xml.dom.XElement

data class NameSpaceContext(
  val defaultNameSpace: String?,
  val prefixes: Map<String, String>,
) {

  companion object {
    fun fromTag(tag: XElement.Tag) = NameSpaceContext(
      defaultNameSpace = tag.attributes["xmlns"],
      prefixes = extractDefinedNameSpaces(tag)
    )

    private fun extractDefinedNameSpaces(tag: XElement.Tag): Map<String, String> {
      val tagPrefix = HashMap<String, String>()
      tag.attributes.forEach { (key, value) ->
        if (key.startsWith("xmlns:")) {
          tagPrefix[key.substring(6)] = value
        }
      }
      return if (tagPrefix.isEmpty()) {
        emptyMap()
      } else {
        tagPrefix
      }
    }
  }

  fun getByPrefix(prefix: String) =
    prefixes[prefix] ?: throw SerializationException("Can't find value of namespace prefix $prefix")

  fun getNameSpace(tag: XElement.Tag): String? {
    val defaultNameSpace = tag.defaultNameSpace
    if (defaultNameSpace != null) {
      return defaultNameSpace
    }
    val elPrefix = tag.prefix
    if (elPrefix != null) {
      return prefixes[elPrefix] ?: throw SerializationException("Can't find value of namespace prefix $elPrefix")
    }
    return null
  }

  fun apply(tag: XElement.Tag): NameSpaceContext {
    val newDefaultNameSpace = tag.attributes["xmlns"]
    val tagPrefix = extractDefinedNameSpaces(tag)
    return when {
      tagPrefix.isEmpty() && newDefaultNameSpace == null -> this
      tagPrefix.isEmpty() && newDefaultNameSpace != null -> copy(defaultNameSpace = newDefaultNameSpace)
      tagPrefix.isNotEmpty() && newDefaultNameSpace == null -> copy(prefixes = prefixes + tagPrefix)
      tagPrefix.isNotEmpty() && newDefaultNameSpace != null -> copy(
        prefixes = prefixes + tagPrefix,
        defaultNameSpace = newDefaultNameSpace
      )

      else -> throw IllegalStateException()
    }
  }
}
