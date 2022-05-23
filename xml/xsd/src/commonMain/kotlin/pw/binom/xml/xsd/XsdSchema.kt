package pw.binom.xml.xsd

import pw.binom.io.AsyncReader
import pw.binom.xml.dom.xmlTree

class XsdSchema {
    companion object {
        const val XS_SCHEMA = "http://www.w3.org/2001/XMLSchema"
        suspend fun parse(reader: AsyncReader) {
            val dom = reader.xmlTree()
            if (dom.tag != "schema" || dom.nameSpace != XS_SCHEMA) {
                TODO()
            }
            dom.childs.forEach {
                if (it.nameSpace == XS_SCHEMA) {
                    when (it.tag) {
                        "annotation" -> XsdAnnotation.read(it)
                        "simpleType" -> XsdSimpleType.parse(requiredName = true, element = it)
                        else -> TODO("Unknown element \"${it.tag}\" ${it.nameSpace}")
                    }
                } else {
                    TODO("Unknown schema \"${it.nameSpace}\" in tag \"${it.tag}\"")
                }
            }
        }
    }
}
