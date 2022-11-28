@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.Attribute
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.serialization.annotations.XmlNode
import pw.binom.xml.serialization.annotations.XmlWrapper
import pw.binom.xml.serialization.annotations.XmlWrapperNamespace

class XmlObjectEncoder(
    val body: XmlElement,
    val classDiscriminator: String,
    override val serializersModule: SerializersModule
) : AbstractEncoder() {

    override fun encodeBoolean(value: Boolean) {
        encodeString(value.toString())
    }

    override fun encodeByte(value: Byte) {
        encodeString(value.toString())
    }

    override fun encodeChar(value: Char) {
        encodeString(value.toString())
    }

    override fun encodeInt(value: Int) {
        encodeString(value.toString())
    }

    override fun encodeDouble(value: Double) {
        encodeString(value.toString())
    }

    override fun encodeFloat(value: Float) {
        encodeString(value.toString())
    }

    override fun encodeLong(value: Long) {
        encodeString(value.toString())
    }

    override fun encodeShort(value: Short) {
        encodeString(value.toString())
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        value ?: return
        encodeSerializableElement(
            index = index,
            descriptor = descriptor,
            serializer = serializer,
            value = value,
        )
    }

    private var descriptor: SerialDescriptor? = null
    private var index: Int = 0

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        this.descriptor = descriptor
        this.index = index
        return true
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val el = XmlElement(
            tag = descriptor.xmlName(index),
            nameSpace = descriptor.xmlNamespace(index)?.getOrNull(0)?.takeIf { it.isNotEmpty() }
        )

        if (descriptor.getElementDescriptor(index).kind is StructureKind.LIST) {
            var store = body

            val wrapper = descriptor.getElementAnnotation<XmlWrapper>(index)?.tag
            val wrapperNs = descriptor.getElementAnnotation<XmlWrapperNamespace>(index)?.ns?.takeIf { it.isNotEmpty() }
            if (wrapper == null && wrapperNs != null) {
                throw IllegalArgumentException(
                    "Can't serialaze ${descriptor.serialName} field ${
                    descriptor.getElementName(
                        index
                    )
                    }. Invalid configuration of XML Namespace"
                )
            }
            if (wrapper != null) {
                store = XmlElement(
                    tag = wrapper,
                    nameSpace = wrapperNs?.getOrNull(0)?.takeIf { it.isNotEmpty() }
                )
                store.parent = body
            }
            val encoder = XmlListEncoder(
                store = store,
                tagName = descriptor.xmlName(index),
                ns = descriptor.xmlNamespace(index)?.getOrNull(0)?.takeIf { it.isNotEmpty() },
                serializersModule = serializersModule,
                classDiscriminator = classDiscriminator,
            )
            serializer.serialize(encoder, value)
        } else {
            val encoder =
                XmlEncoder(root = el, serializersModule = serializersModule, classDiscriminator = classDiscriminator)
            serializer.serialize(encoder, value)
            el.parent = body
        }
    }

    override fun encodeString(value: String) {
        val descriptor = descriptor
        if (descriptor != null) {
            this.descriptor = null
            val wrapper = descriptor.getElementAnnotation<XmlNode>(index)

            if (wrapper != null) {
                val e = XmlElement(
                    tag = descriptor.xmlName(index),
                    nameSpace = descriptor.xmlNamespace(index)?.getOrNull(0)?.takeIf { it.isNotEmpty() }
                )
                e.body = value
                e.parent = body
            } else {
                body.attributes[
                    Attribute(
                        nameSpace = descriptor.xmlNamespace(index)?.getOrNull(0)?.takeIf { it.isNotEmpty() },
                        name = descriptor.xmlName(index)
                    )
                ] = value
            }
        }
    }
}
