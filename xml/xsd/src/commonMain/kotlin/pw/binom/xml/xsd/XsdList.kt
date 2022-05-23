package pw.binom.xml.xsd

import pw.binom.xml.dom.XmlElement

class XsdList(
    var id: String?,
    var itemType: String?,
    var annotation: XsdAnnotation?,
    val types: MutableList<XsdSimpleType>,
) {
    companion object {
        fun parse(element: XmlElement): XsdList {
            val id = element.attributes.findXsd("id")
            val itemType = element.attributes.findXsd("itemType")
            var annotation: XsdAnnotation? = null
            val types = ArrayList<XsdSimpleType>()
            element.childs.forEachXsd {
                when (it.tag) {
                    "annotation" -> annotation = XsdAnnotation.read(it)
                    "simpleType" -> types += XsdSimpleType.parse(requiredName = false, element = it)
                }
            }
            return XsdList(
                id = id,
                itemType = itemType,
                annotation = annotation,
                types = types,
            )
        }
    }
}
