package pw.binom.xml.xsd

import pw.binom.net.URL
import pw.binom.net.toURL
import pw.binom.xml.dom.XmlElement

class XsdDocumentation(val source: URL?) : XsdAnnotation.AnnotationElement {
    companion object {
        fun parse(element: XmlElement) = XsdDocumentation(
            source = element.attributes.findXsd("source")?.toURL()
        )
    }
}
