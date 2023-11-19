package pw.binom.xml.xsd

import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.find

class XsdRestriction(
  var minLength: Int?,
  var maxLength: Int?,
  var maxInclusive: Int?,
  var pattern: String?,
) {
  companion object {
    fun parse(element: XmlElement): XsdRestriction {
      var minLength: Int? = null
      var maxLength: Int? = null
      var maxInclusive: Int? = null
      var pattern: String? = null
      val baseOn = element.attributes.find("base", XsdSchema.XS_SCHEMA)
      element.childs.forEachXsd {
        when (it.tag) {
          "minLength" -> minLength = it.attributes.find("value", XsdSchema.XS_SCHEMA)!!.toInt()
          "maxLength" -> maxLength = it.attributes.find("value", XsdSchema.XS_SCHEMA)!!.toInt()
          "maxInclusive" -> maxInclusive = it.attributes.find("value", XsdSchema.XS_SCHEMA)!!.toInt()
          "pattern" -> pattern = it.attributes.find("value", XsdSchema.XS_SCHEMA)!!
          else -> TODO("Unknown element \"${it.tag}\" ${it.nameSpace}")
        }
      }
      return XsdRestriction(
        minLength = minLength,
        maxLength = maxLength,
        pattern = pattern,
        maxInclusive = maxInclusive,
      )
    }
  }
}
