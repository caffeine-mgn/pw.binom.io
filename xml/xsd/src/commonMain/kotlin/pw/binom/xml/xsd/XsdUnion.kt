package pw.binom.xml.xsd

import pw.binom.xml.dom.XmlElement

class XsdUnion(
    val annotation: XsdAnnotation?,
    val types: MutableList<XsdSimpleType>,
) {
    companion object {
        fun parse(element: XmlElement): XsdUnion {
            var annotation: XsdAnnotation? = null
            val types = ArrayList<XsdSimpleType>()
            element.childs.forEachXsd {
                when (it.tag) {
                    "annotation" -> annotation = XsdAnnotation.read(it)
                    "simpleType" -> types += XsdSimpleType.parse(requiredName = false, element = it)
                }
            }
            return XsdUnion(
                annotation = annotation,
                types = types,
            )
        }
    }
}

sealed interface XsdTypeReference {
    val name: String
    val nameSpace: String?
}

interface XsdSimpleTypeReference : XsdTypeReference
