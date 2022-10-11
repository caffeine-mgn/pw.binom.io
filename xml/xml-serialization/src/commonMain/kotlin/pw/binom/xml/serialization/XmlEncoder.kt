package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

class XmlEncoder(root: XmlElement?, val classDiscriminator: String, override val serializersModule: SerializersModule) :
    AbstractEncoder() {
    var root: XmlElement? = root
        private set

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val el = if (root == null) {
            val xmlName = descriptor.xmlName()
            val xmlNamespace = descriptor.xmlNamespace()?.getOrNull(0)?.takeIf { it.isNotEmpty() }
            val tag = XmlElement(
                tag = xmlName,
                nameSpace = xmlNamespace,
            )
            root = tag
            tag
        } else {
            root!!
        }
        return XmlObjectEncoder(
            body = el,
            serializersModule = serializersModule,
            classDiscriminator = classDiscriminator,
        )
    }

    override fun encodeBoolean(value: Boolean) {
        encodeString(value.toString())
    }

    override fun encodeByte(value: Byte) {
        encodeString(value.toString())
    }

    override fun encodeChar(value: Char) {
        encodeString(value.toString())
    }

    override fun encodeDouble(value: Double) {
        encodeString(value.toString())
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeFloat(value: Float) {
        encodeString(value.toString())
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeInt(value: Int) {
        encodeString(value.toString())
    }

    override fun encodeLong(value: Long) {
        encodeString(value.toString())
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        TODO("Not yet implemented")
    }

    override fun encodeShort(value: Short) {
        encodeString(value.toString())
    }

    override fun encodeString(value: String) {
        root!!.body = value
    }
}
