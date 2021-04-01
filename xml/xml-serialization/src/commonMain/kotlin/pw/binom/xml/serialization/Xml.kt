package pw.binom.xml.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.io.StringReader
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.dom.xmlTree
import pw.binom.xml.sax.SyncXmlRootWriterVisitor

class Xml(val serializersModule: SerializersModule = EmptySerializersModule, val classDiscriminator: String = "type") {
    fun <T : Any> encodeToXmlElement(serializer: KSerializer<T>, value: T): XmlElement {
        val encoder =
            XmlEncoder(root = null, serializersModule = serializersModule, classDiscriminator = classDiscriminator)
        serializer.serialize(encoder, value)
        return encoder.root!!
    }

    fun <T : Any> encodeToString(serializer: KSerializer<T>, value: T): String {
        val sb = StringBuilder()
        encodeToXmlElement(serializer, value).accept(SyncXmlRootWriterVisitor(sb))
        return sb.toString()
    }

    fun <T : Any> decodeFromXmlElement(serializer: KSerializer<T>, xmlElement: XmlElement): T =
        serializer.deserialize(XmlDecoder(xmlElement, serializersModule))

    fun <T : Any> decodeFromString(serializer: KSerializer<T>, xml: String): T =
        decodeFromXmlElement(serializer, StringReader(xml).xmlTree()!!)
}
