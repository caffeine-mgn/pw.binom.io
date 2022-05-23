package pw.binom.xml.xsd

import pw.binom.xml.dom.XmlElement

class XsdAnnotation(
    val id: String?,
    val elements: ArrayList<AnnotationElement>
) : XsdElement {
    sealed interface AnnotationElement

    companion object {
        fun read(element: XmlElement): XsdAnnotation {
            val id = element.attributes.findXsd("id")
            val elements = ArrayList<AnnotationElement>()
            element.childs.forEachXsd {
                when (it.tag) {
                    "documentation" -> elements += XsdDocumentation.parse(it)
                    "appinfo" -> elements += XsdAppinfo.parse(it)
                    else -> TODO("Unknown element \"${it.tag}\" ${it.nameSpace}")
                }
            }
            return XsdAnnotation(
                id = id,
                elements = elements,
            )
        }
    }
}
