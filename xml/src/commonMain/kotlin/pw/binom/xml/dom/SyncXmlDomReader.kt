package pw.binom.xml.dom

import pw.binom.collections.defaultMutableMap
import pw.binom.io.Reader
import pw.binom.io.StringReader
import pw.binom.io.asAsync
import pw.binom.xml.XML_NAMESPACE_PREFIX
import pw.binom.xml.XML_NAMESPACE_PREFIX_WITH_DOTS
import pw.binom.xml.sax.AsyncXmlReaderVisitor
import pw.binom.xml.sax.SyncXmlVisitor

class SyncXmlDomReader private constructor(private val ctx: NameSpaceContext, tag: String) : SyncXmlVisitor {
  class NameSpaceContext(var pool: MutableMap<String, String> = defaultMutableMap()) {
    var default: String? = null
    private var autoIterator = 0

    /**
     * key - url
     * value - prefix
     */
    var prefix = defaultMutableMap<String, String>()

    fun pool(uri: String) = pool.getOrPut(uri) { "ns${autoIterator++}" }

    fun copy(): NameSpaceContext {
      val c = NameSpaceContext(pool)
      c.default = default
      c.prefix.putAll(prefix)
      return c
    }
  }

  constructor(tag: String) : this(ctx = NameSpaceContext(), tag = tag)
  constructor() : this(tag = "")

  val rootNode = XmlElement(
    tag = tag,
    nameSpace = null,
  )

  override fun attribute(name: String, value: String?) {
    if (name == XML_NAMESPACE_PREFIX && value != null) {
      ctx.default = ctx.pool(value)
      return
    }
    if (name.startsWith(XML_NAMESPACE_PREFIX_WITH_DOTS) && value != null) {
      ctx.prefix[name.removePrefix(XML_NAMESPACE_PREFIX_WITH_DOTS)] = ctx.pool(value)
      return
    }
    rootNode.attributes[Attribute(null, name)] = value
  }

  override fun cdata(body: String) {
    rootNode.body = body
    rootNode.cdata = true
  }

  private fun fixCurrentNS() {
    if (rootNode.nameSpace == null) {
      if (":" !in rootNode.tag) {
        rootNode.nameSpace = ctx.default
      } else {
        val i = rootNode.tag.indexOf(":")
        val prefix = rootNode.tag.substring(0, i)
        val ns = ctx.prefix[prefix] ?: throw RuntimeException("Can't find prefix \"$prefix\"")
        rootNode.nameSpace = ns
        rootNode.tag = rootNode.tag.substring(i + 1)
      }
    }

    rootNode.attributes.forEach {
      if (it.key.nameSpace == null) {
        val i = it.key.name.indexOf(":")
        if (i == -1) {
          it.key.nameSpace = rootNode.nameSpace ?: ctx.default
        } else {
          val prefix = it.key.name.substring(0, i)
          val ns = ctx.prefix[prefix] ?: throw RuntimeException("Can't find prefix \"$prefix\"")
          it.key.nameSpace = ns
          it.key.name = it.key.name.substring(i + 1)
        }
      }
    }
  }

  override fun end() {
    fixCurrentNS()
  }

  override fun start(tagName: String) {
  }

  override fun subNode(name: String): SyncXmlVisitor {
    fixCurrentNS()
    val r = SyncXmlDomReader(ctx.copy(), name)
    r.rootNode.parent = rootNode
    return r
  }

  override fun value(body: String) {
    rootNode.body = body
  }
}

fun Reader.xmlTree(withHeader: Boolean = false): XmlElement {
  return a {
    val r = AsyncXmlDomReader("")
    AsyncXmlReaderVisitor(this.asAsync()).accept(r)
    val element = r.rootNode.childs.get(0)
    element.parent = null
    element
  }
}

// <?xml version="1.0" encoding="UTF-8"?>
fun String.xmlTree(withHeader: Boolean = false): XmlElement {
  var str = this
  if (withHeader) {
    if (!startsWith("<?xml")) {
      throw IllegalArgumentException("Xml not starts with header")
    }
    val p = indexOf("?>")
    if (p == -1) {
      throw IllegalArgumentException("Invalid Xml: can't find end of header")
    }
    str = substring(p + 2)
  }
  return StringReader(str).xmlTree(withHeader = false)
}
