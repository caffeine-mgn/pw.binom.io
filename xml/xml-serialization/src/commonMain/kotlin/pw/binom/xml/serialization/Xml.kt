package pw.binom.xml.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.io.asAsync
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.xmlTree
import pw.binom.xml.sax.AsyncXmlRootWriterVisitor
import pw.binom.xml.serialization.annotations.XmlName

class Xml(
    val serializersModule: SerializersModule = EmptySerializersModule(),
    val classDiscriminator: String = "type"
) {
    fun <T : Any> encodeToXmlElement(serializer: KSerializer<T>, value: T): XmlElement {
        val encoder =
            XmlEncoder(root = null, serializersModule = serializersModule, classDiscriminator = classDiscriminator)
        serializer.serialize(encoder, value)
        return encoder.root!!
    }

    fun <T : Any> encodeToString(serializer: KSerializer<T>, value: T): String {
        val sb = StringBuilder()
        val tagName = serializer.descriptor.annotations.find { it is XmlName }?.let { it as XmlName }?.name
            ?: serializer.descriptor.serialName
        val v = AsyncXmlRootWriterVisitor.withoutHeader(sb.asAsync())
//        val v = SyncXmlRootWriterVisitor(sb)
        val b = encodeToXmlElement(serializer, value)
        b.nameSpace = serializer.descriptor.xmlNamespace()
        val root = XmlElement()
        b.parent = root
        a {
            root.accept(v)
        }
        return sb.toString()
    }

    fun <T : Any> decodeFromXmlElement(serializer: KSerializer<T>, xmlElement: XmlElement): T {
        if (xmlElement.tag != serializer.descriptor.xmlName()) {
            throw SerializationException("Can't decode to ${serializer.descriptor.serialName}: invalid xml tag ${xmlElement.tag}")
        }
        if (xmlElement.nameSpace != serializer.descriptor.xmlNamespace()) {
            val expected = serializer.descriptor.xmlNamespace()?.let { "\"$it\"" } ?: "none"
            val actual = xmlElement.nameSpace?.let { "\"$it\"" } ?: "none"
            throw SerializationException(
                "Can't decode to ${serializer.descriptor.serialName}: Expected $expected, actual $actual"
            )
        }
        return serializer.deserialize(XmlDecoder(xmlElement, serializersModule))
    }

    fun <T : Any> decodeFromString(serializer: KSerializer<T>, xml: String): T =
        decodeFromXmlElement(serializer, xml.xmlTree()!!)
}
