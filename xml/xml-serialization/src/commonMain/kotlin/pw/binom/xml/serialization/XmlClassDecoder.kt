package pw.binom.xml.serialization

import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XElement
import pw.binom.xml.serialization.annotations.XmlAttribute
import pw.binom.xml.serialization.annotations.XmlNamespace
import pw.binom.xml.serialization.annotations.XmlWrapper
import pw.binom.xml.tags

class XmlClassDecoder(
  val element: XElement.Tag,
  val descriptor: SerialDescriptor,
  val context: NameSpaceContext,
  override val serializersModule: SerializersModule,
  val config: XmlConfig,
) : AbstractDecoder() {

  sealed interface Element {
    class Attr(val index: Int, value: String) : Element
    class Node(val index: Int) : Element
    class Wrapper(val tag: String)
  }

  init {
    val rr = ArrayList<Element>()
    descriptor.eachElements { index ->
      val attr = descriptor.getElementAnnotation<XmlAttribute>(index)
      val name = descriptor.xmlName(index)

      if (attr != null) {
        val attrValue = if (attr.nameSpace.isEmpty()) {
          element.attributes[name]
        } else {
          attr.nameSpace.asSequence()
            .mapNotNull { uri ->
              context.prefixes.entries.find { it.value == uri }?.key
            }
            .map { "$it:$name" }
            .mapNotNull { element.attributes[it] }
            .firstOrNull()
        } ?: return@eachElements
        rr += Element.Attr(index, attrValue)
        return@eachElements
      }
      val wrapper = descriptor.getElementAnnotation<XmlWrapper>(index)
      if (wrapper != null) {

      }

    }

  }

  private var nextField = -1
  val attributes = element.attributes.iterator()
  private val tags = element.tags().iterator()
  private var attrValue: String? = null


  override fun decodeString(): String {
    return attrValue!!
  }

  private fun readAttribute(name: String, value: String): Int {
    val index = name.indexOf(':')
    val (nameSpace, attName) = if (index == -1) {
      null to name
    } else {
      context.getByPrefix(name.substring(0, index)) to name.substring(index + 1)
    }
    descriptor.eachElements { index ->
      val att = descriptor.getElementAnnotation<XmlAttribute>(index) ?: return@eachElements
      val name = descriptor.xmlName(index)
      if (attName != name) {
        return@eachElements
      }

      if ((nameSpace != null && (nameSpace in att.nameSpace)) || (nameSpace == null && att.nameSpace.isEmpty())) {
        nextField = index
        this.attrValue = value
        return index
      }
    }
    return CompositeDecoder.UNKNOWN_NAME
  }

  private fun readTag(tag: XElement.Tag): Int {
    val tagNameSpace = context.getNameSpace(tag)
    val tagName = tag.nameWithoutPrefix
    descriptor.eachElements { index ->
      val att = descriptor.getElementAnnotation<XmlAttribute>(index)
      if (att != null) {
        return@eachElements
      }
      if (tagName != descriptor.xmlName(index)) {
        return@eachElements
      }
      val elementNameSpaces = descriptor.getElementAnnotation<XmlNamespace>(index)?.ns ?: emptyArray()
      if (tagNameSpace == null && elementNameSpaces.isEmpty()) {
        return index
      }
      if (tagNameSpace == null && elementNameSpaces.isNotEmpty()) {
        return@eachElements
      }
    }
    return CompositeDecoder.UNKNOWN_NAME
  }

  private var cursor = -1
  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    while (cursor < descriptor.elementsCount) {
      cursor++
      val attr = descriptor.getElementAnnotation<XmlAttribute>(cursor)
      val name = descriptor.xmlName(cursor)
      if (attr != null) {
        if (attr.nameSpace.isEmpty()) {
          if (element.attributes.containsKey(name)) {
            return cursor
          }
        } else {
          val attrFound = attr.nameSpace.asSequence()
            .mapNotNull { uri ->
              context.prefixes.entries.find { it.value == uri }?.key
            }
            .map { "$it:$name" }
            .any { element.attributes.containsKey(it) }
          if (attrFound) {
            return cursor
          }
        }
      }
    }
    return CompositeDecoder.DECODE_DONE
  }
  /*
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
      while (true) {
        if (!attributes.hasNext() && !tags.hasNext()) {
          return CompositeDecoder.DECODE_DONE
        }
        if (attributes.hasNext()) {
          val (key, attValue) = attributes.next()
          val r = readAttribute(name = key, value = attValue)
          if (r >= 0) {
            return r
          }
        }
        if (tags.hasNext()) {
          val tag = tags.next()
          val r = readTag(tag)
          if (r >= 0) {
            return r
          }
        }
        if (config.ignoreUnknownKey) {
          continue
        } else {
          return CompositeDecoder.UNKNOWN_NAME
        }
      }
    }
    */
}
