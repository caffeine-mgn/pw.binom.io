package pw.binom.xml.xsd

import pw.binom.net.URL
import pw.binom.net.toURL
import pw.binom.xml.dom.XmlElement

class XsdAppinfo(source: URL?) : XsdAnnotation.AnnotationElement {
    companion object {
        fun parse(element: XmlElement) = XsdAppinfo(
            source = element.attributes.findXsd("source")?.toURL()
        )
    }
}
