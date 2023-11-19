package pw.binom.xml.xsd

import pw.binom.xml.dom.Attribute
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.find

inline fun List<XmlElement>.forEachXsd(element: (XmlElement) -> Unit) {
  forEach {
    if (it.nameSpace == XsdSchema.XS_SCHEMA) {
      element(it)
    } else {
      TODO("Unknown schema \"${it.nameSpace}\" in tag \"${it.tag}\"")
    }
  }
}

fun Map<Attribute, String?>.findXsd(name: String): String? =
  find(name = name, nameSpace = XsdSchema.XS_SCHEMA)
