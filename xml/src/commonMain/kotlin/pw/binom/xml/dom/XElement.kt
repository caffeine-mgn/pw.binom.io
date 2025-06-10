package pw.binom.xml.dom

import pw.binom.xml.sax.SyncXmlVisitor2
import kotlin.jvm.JvmInline

sealed class XElement {
  var parent: Tag? = null
    set(value) {
      if (value === field) {
        return
      }
      field?.privateChild?.remove(this)
      field = value
      field?.privateChild?.add(this)
    }

  abstract fun accept(visitor: SyncXmlVisitor2)

  data class Text(val text: String) : XElement() {
    override fun toString(): String = text
    override fun accept(visitor: SyncXmlVisitor2) {
      visitor.text(text)
    }
  }

  data class CDATA(val text: String) : XElement() {
    override fun toString() =
      "<![CDATA[$text]]>"

    override fun accept(visitor: SyncXmlVisitor2) {
      visitor.cdata(text)
    }
  }

  data class Comment(val text: String) : XElement() {
    override fun toString() = "<!--$text-->"
    override fun accept(visitor: SyncXmlVisitor2) {
      visitor.comment(text)
    }
  }

  data class Config(var name: String) : XElement() {
    val attributes = LinkedHashMap<String, String>()
    override fun accept(visitor: SyncXmlVisitor2) {
      visitor.startConfig(name)
      attributes.forEach {
        visitor.attribute(it.key, it.value)
      }
      visitor.endConfig()
    }
  }

  class Tag(var name: String) : XElement() {
    val prefix: String?
      get() {
        val nameSpaceSeparator = name.indexOf(':')
        if (nameSpaceSeparator == -1) {
          return null
        }
        return name.substring(0, nameSpaceSeparator)
      }
    val nameWithoutPrefix: String
      get() {
        val nameSpaceSeparator = name.indexOf(':')
        if (nameSpaceSeparator == -1) {
          return name
        }
        return name.substring(nameSpaceSeparator + 1)
      }

    fun setNameSpacePrefix(prefix: String, url: String) {
      attributes["xmlns:$prefix"] = url
    }

    fun getDefinedNameSpaces(): Map<String, String> {
      val result = HashMap<String, String>()
      attributes.forEach { (key, value) ->
        if (key.startsWith("xmlns:")) {
          result[key.substring(6)] = value
        }
      }
      if (result.isEmpty()) {
        return emptyMap()
      }
      return result
    }

    var defaultNameSpace: String?
      get() = attributes["xmlns"]
      set(value) {
        if (value == null) {
          attributes.remove("xmlns")
        } else {
          attributes["xmlns"] = value
        }
      }
    internal val privateChild = ArrayList<XElement>()
    val attributes = LinkedHashMap<String, String>()
    val child: List<XElement>
      get() = privateChild

    override fun toString(): String {
      val sb = StringBuilder()
      sb.append("<").append(name)
      attributes.forEach { (attrName, attrValue) ->
        sb.append(" ").append(attrName).append("=\"").append(attrValue).append("\"")
      }
      if (child.isEmpty()) {
        sb.append("/>")
      } else {
        sb.append(">")
        child.forEach {
          sb.append(it.toString())
        }
        sb.append("</$name>")
      }
      return sb.toString()
    }

    override fun accept(visitor: SyncXmlVisitor2) {
      visitor.startOpenTag(tagName = name)
      if (child.isEmpty()) {
        visitor.endOpenTag(true)
      } else {
        visitor.endOpenTag(false)
        child.forEach { child -> child.accept(visitor) }
        visitor.endTag(tagName = name)
      }
    }
  }
}
