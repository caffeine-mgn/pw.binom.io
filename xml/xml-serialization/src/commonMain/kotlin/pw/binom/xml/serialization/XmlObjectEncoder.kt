package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.Attribute
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.serialization.annotations.*

class XmlObjectEncoder(
    val body: XmlElement,
    val classDiscriminator: String,
    override val serializersModule: SerializersModule
) : CompositeEncoder {
    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeStringElement(descriptor, index, value.toString())
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeStringElement(descriptor, index, value.toString())
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        TODO("Not yet implemented")
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val el = XmlElement(
            tag = descriptor.xmlName(index),
            nameSpace = descriptor.xmlNamespace(index)
        )

        if (descriptor.getElementDescriptor(index).kind is StructureKind.LIST) {
            var store = body

            val wrapper = descriptor.getElementAnnotation<XmlWrapper>(index)?.tag
            val wrapperNs = descriptor.getElementAnnotation<XmlWrapperNamespace>(index)?.ns
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
                    nameSpace = wrapperNs
                )
                store.parent = body
            }
            val encoder = XmlListEncoder(
                store = store,
                tagName = descriptor.xmlName(index),
                ns = descriptor.xmlNamespace(index),
                serializersModule = serializersModule,
            )
            serializer.serialize(encoder, value)
        } else {
            val encoder =
                XmlEncoder(root = el, serializersModule = serializersModule, classDiscriminator = classDiscriminator)
            serializer.serialize(encoder, value)
            el.parent = body
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeStringElement(descriptor, index, value.toString())
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        val wrapper = descriptor.getElementAnnotation<XmlNode>(index)

        if (wrapper != null) {
            val e = XmlElement(
                tag = descriptor.xmlName(index),
                nameSpace = descriptor.xmlNamespace(index)
            )
            e.body = value
            e.parent = body
        } else {
            body.attributes[Attribute(
                nameSpace = descriptor.xmlNamespace(index),
                name = descriptor.xmlName(index)
            )] = value
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {

    }
}

fun SerialDescriptor.xmlName() =
    (annotations.find { it is XmlName } as XmlName?)?.name ?: serialName

fun SerialDescriptor.xmlName(index: Int) =
    (getElementAnnotations(index).find { it is XmlName } as XmlName?)?.name
        ?: getElementName(index)

fun SerialDescriptor.xmlNamespace() =
    (annotations.find { it is XmlNamespace } as XmlNamespace?)?.ns

fun SerialDescriptor.xmlNamespace(index: Int) =
    (getElementAnnotations(index).find { it is XmlNamespace } as XmlNamespace?)?.ns

inline fun <reified T : Any> SerialDescriptor.getElementAnnotation(index: Int) =
    (getElementAnnotations(index).find { it is T } as T?)