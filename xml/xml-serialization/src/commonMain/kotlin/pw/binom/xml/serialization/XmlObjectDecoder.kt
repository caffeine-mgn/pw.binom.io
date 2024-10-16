package pw.binom.xml.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement
import pw.binom.xml.serialization.annotations.XmlNode
import pw.binom.xml.serialization.annotations.XmlWrapper
import pw.binom.xml.serialization.annotations.XmlWrapperNamespace

class XmlObjectDecoder(val root: XmlElement, override val serializersModule: SerializersModule) : CompositeDecoder {
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val str = decodeStringElement(descriptor, index)
        return when (str) {
            "true" -> true
            "false" -> false
            else -> throw SerializationException("Can't convert \"$str\" to Boolean")
        }
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        val str = decodeStringElement(descriptor, index)
        return str.toByteOrNull() ?: throw SerializationException("Can't convert \"$str\" to Byte")
    }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        val str = decodeStringElement(descriptor, index)
        if (str.length != 1) {
            throw SerializationException("Can't convert \"$str\" to Char")
        }
        return str[0]
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        val str = decodeStringElement(descriptor, index)
        return str.toDoubleOrNull() ?: throw SerializationException("Can't convert \"$str\" to Double")
    }

    var cursor = -1
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (cursor + 1 == descriptor.elementsCount) {
            return CompositeDecoder.DECODE_DONE
        }
        return ++cursor
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        val str = decodeStringElement(descriptor, index)
        return str.toFloatOrNull() ?: throw SerializationException("Can't convert \"$str\" to Float")
    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO("Not yet implemented")
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        val str = decodeStringElement(descriptor, index)
        return str.toIntOrNull() ?: throw SerializationException("Can't convert \"$str\" to Int")
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        val str = decodeStringElement(descriptor, index)
        return str.toLongOrNull() ?: throw SerializationException("Can't convert \"$str\" to Long")
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        TODO("Not yet implemented")
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val name = descriptor.xmlName(index)
        val namespace = descriptor.xmlNamespace(index)
        var from = root
        if (descriptor.getElementDescriptor(index).kind is StructureKind.LIST) {
            val wrapper = descriptor.getElementAnnotation<XmlWrapper>(index)?.tag
            val wrapperNs = descriptor.getElementAnnotation<XmlWrapperNamespace>(index)?.ns
            if (wrapper != null) {
                from =
                    from.childs.find { it.tag == wrapper && it.nameSpace == wrapperNs } ?: throw SerializationException(
                        "Wrapper not found"
                    )
                return deserializer.deserialize(
                    XmlListDecoder(
                        storage = from,
                        serializersModule = serializersModule,
                        tagName = name,
                        nameSpace = namespace,
                    )
                )

            }


        }
        val el = root.childs.find { it.tag == name && it.nameSpace == namespace }
            ?: throw SerializationException("Not found ${descriptor.serialName}::${descriptor.getElementName(index)}")
        return deserializer.deserialize(XmlDecoder(el, serializersModule))
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        val str = decodeStringElement(descriptor, index)
        return str.toShortOrNull() ?: throw SerializationException("Can't convert \"$str\" to Short")
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val name = descriptor.xmlName(index)
        val ns = descriptor.xmlNamespace(index)
        val isWrapped = descriptor.getElementAnnotation<XmlNode>(index) != null
        fun genErr(): Nothing =
            throw SerializationException(
                "Can't find element ${descriptor.serialName}::${descriptor.getElementName(index)}"
            )
        return if (isWrapped) {
            root.childs.find { it.tag == name && it.nameSpace == ns }?.body ?: genErr()
        } else {
            root.attributes.entries.find { it.key.name == name && it.key.nameSpace == ns }?.value ?: genErr()
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }
}