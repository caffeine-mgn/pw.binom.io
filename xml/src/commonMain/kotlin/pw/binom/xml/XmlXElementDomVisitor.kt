package pw.binom.xml

import pw.binom.collections.LinkedList
import pw.binom.xml.dom.XElement
import pw.binom.xml.sax.XMLSAXException
import pw.binom.xml.sax.XmlVisitor

class XmlXElementDomVisitor : XmlVisitor {
  private val roots = ArrayList<XElement>()
  private val tagStack = LinkedList<XElement.Tag>()
  private var currentConfig: XElement.Config? = null
  val elements: List<XElement>
    get() = roots

  private var tagFullDefined = true

  override fun startOpenTag(tagName: String) {
    val last = tagStack.lastOrNull()
    val newTag = XElement.Tag(tagName)
    if (last == null) {
      roots += newTag
    } else {
      newTag.parent = last
    }
    tagStack.addLast(newTag)
    tagFullDefined = false
  }

  override fun endOpenTag(endTag: Boolean) {
    tagFullDefined = true
    if (endTag) {
      tagStack.removeLast()
    }
  }

  override fun endTag(tagName: String) {
    val lastTag = tagStack.removeLast()
    if (lastTag.name != tagName) {
      throw XMLSAXException("Unexpected tag name \"$tagName\". Shhould be \"${lastTag.name}\"")
    }
  }

  override fun endConfig() {
    add(currentConfig!!)
  }

  override fun startConfig(name: String) {
    currentConfig = XElement.Config(name)
  }

  override fun attribute(name: String, value: String) {
    val currentConfig = currentConfig
    if (currentConfig != null) {
      currentConfig.attributes[name] = value
    } else {
      tagStack.last().attributes[name] = value
    }
  }

  override fun comment(body: String) {
    add(XElement.Comment(body))
  }

  override fun cdata(body: String) {
    add(XElement.CDATA(body))
  }

  override fun text(body: String) {
    if (!tagFullDefined) {
      return
    }
    add(XElement.Text(body))
  }

  private fun add(element: XElement) {
    val current = tagStack.lastOrNull()
    if (current == null) {
      roots += element
    } else {
      element.parent = tagStack.last()
    }
  }
}
