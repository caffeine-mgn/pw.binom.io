package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

class XmlEncoder(root: XmlElement?, val classDiscriminator: String, override val serializersModule: SerializersModule) :
    Encoder {
    var root: XmlElement? = root
        private set

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val el = if (root == null) {
            val xmlName = descriptor.xmlName()
            val xmlNamespace = descriptor.xmlNamespace()
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
        TODO("Not yet implemented")
    }

    override fun encodeByte(value: Byte) {
        TODO("Not yet implemented")
    }

    override fun encodeChar(value: Char) {
        TODO("Not yet implemented")
    }

    override fun encodeDouble(value: Double) {
        TODO("Not yet implemented")
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeFloat(value: Float) {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeInt(value: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeLong(value: Long) {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        TODO("Not yet implemented")
    }

    override fun encodeShort(value: Short) {
        TODO("Not yet implemented")
    }

    override fun encodeString(value: String) {
        root!!.body = value
    }
}
