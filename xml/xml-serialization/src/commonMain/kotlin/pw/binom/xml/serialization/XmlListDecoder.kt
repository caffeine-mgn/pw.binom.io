package pw.binom.xml.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

class XmlListDecoder(
    val storage: XmlElement,
    val tagName: String,
    val nameSpace: Array<String>?,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {
    val elements = storage.childs.filter { it.tag == tagName && it.nameSpace.inArray(nameSpace) }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return this
    }

    override fun decodeString(): String {
        val element = elements[cursor]
        return element.body ?: throw SerializationException()
    }

    private var cursor = -1

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = elements.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (cursor + 1 == elements.size) {
            return CompositeDecoder.DECODE_DONE
        }
        return ++cursor
    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO("Not yet implemented")
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        val element = elements[cursor]

        return if (deserializer.descriptor.kind is StructureKind.LIST) {
            throw SerializationException("Not support list in list")
        } else {
            val decoder = XmlObjectDecoder(
                root = element,
                descriptor = deserializer.descriptor,
                serializersModule = serializersModule
            )
            deserializer.deserialize(decoder)
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }
}
