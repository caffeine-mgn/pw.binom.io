package pw.binom.xml.xsd

import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.find

class XsdSimpleType(
  val schema: String?,
  val id: String?,
  val name: String?,
  var annotation: XsdAnnotation?,
  var restriction: XsdRestriction?,
  var union: XsdUnion?,
  var list: XsdList?,
) {
  companion object {
    fun parse(requiredName: Boolean, element: XmlElement): XsdSimpleType {
      val id = element.attributes.find("name", XsdSchema.XS_SCHEMA)
      val name = element.attributes.find("name", XsdSchema.XS_SCHEMA)
      if (name == null && requiredName) {
        TODO("Attribute \"name\" missing")
      }
      var annotation: XsdAnnotation? = null
      var restriction: XsdRestriction? = null
      var list: XsdList? = null
      var union: XsdUnion? = null
      element.childs.forEachXsd {
        when (it.tag) {
          "annotation" -> annotation = XsdAnnotation.read(it)
          "restriction" -> {
            if (union != null) {
              TODO("SimpleType already has union")
            }
            if (list != null) {
              TODO("SimpleType already has list")
            }
            restriction = XsdRestriction.parse(it)
          }

          "union" -> {
            if (restriction != null) {
              TODO("SimpleType already has restriction")
            }
            if (list != null) {
              TODO("SimpleType already has list")
            }
            union = XsdUnion.parse(it)
          }

          "list" -> {
            if (restriction != null) {
              TODO("SimpleType already has restriction")
            }
            if (union != null) {
              TODO("SimpleType already has union")
            }
            list = XsdList.parse(it)
          }

          else -> TODO("Unknown element \"${it.tag}\" ${it.nameSpace}")
        }
      }

      return XsdSimpleType(
        id = id,
        name = name,
        schema = null,
        annotation = annotation,
        restriction = restriction,
        union = union,
        list = list,
      )
    }
  }
}
