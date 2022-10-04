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
    val nameSpace: String?,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {
    val elements = storage.childs.filter { it.tag == tagName && it.nameSpace == nameSpace }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return this
    }

    override fun decodeBoolean(): Boolean {
        TODO("Not yet implemented")
    }

    override fun decodeByte(): Byte {
        TODO("Not yet implemented")
    }

    override fun decodeChar(): Char {
        TODO("Not yet implemented")
    }

    override fun decodeDouble(): Double {
        TODO("Not yet implemented")
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeFloat(): Float {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        TODO("Not yet implemented")
    }

    override fun decodeInt(): Int {
        TODO("Not yet implemented")
    }

    override fun decodeLong(): Long {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        TODO("Not yet implemented")
    }

    override fun decodeShort(): Short {
        TODO("Not yet implemented")
    }

    override fun decodeString(): String {
        val element = elements[cursor]
        return element.body ?: throw SerializationException()
    }

//    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
//        TODO("Not yet implemented")
//    }
//
//    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
//        TODO("Not yet implemented")
//    }
//
//    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
//        TODO("Not yet implemented")
//    }

    private var cursor = -1

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = elements.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (cursor + 1 == elements.size) {
            return CompositeDecoder.DECODE_DONE
        }
        return ++cursor
    }

//    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
//        TODO("Not yet implemented")
//    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO("Not yet implemented")
    }

//    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
//        TODO("Not yet implemented")
//    }

//    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
//        TODO("Not yet implemented")
//    }

//    @ExperimentalSerializationApi
//    override fun <T : Any> decodeNullableSerializableElement(
//        descriptor: SerialDescriptor,
//        index: Int,
//        deserializer: DeserializationStrategy<T?>,
//        previousValue: T?
//    ): T? {
//        TODO("Not yet implemented")
//    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        val element = elements[cursor]

        return if (deserializer.descriptor.kind is StructureKind.LIST) {
            throw SerializationException("Not support list in list")
//            val from = element
//            val wrapper = descriptor.getElementAnnotation<XmlWrapper>(cursor)?.tag
//            val wrapperNs = descriptor.getElementAnnotation<XmlWrapperNamespace>(cursor)?.ns
//            if (wrapper != null) {
//                from =
//                    from.childs.find { it.tag == wrapper && it.nameSpace == wrapperNs } ?: throw SerializationException(
//                        "Wrapper not found"
//                    )
//            }
//            return deserializer.deserialize(
//                XmlListDecoder(
//                    storage = from,
//                    serializersModule = serializersModule,
//                    tagName = name,
//                    nameSpace = namespace,
//                )
//            )
        } else {
            val decoder = XmlObjectDecoder(
                root = element,
                descriptor = deserializer.descriptor,
                serializersModule = serializersModule
            )
            deserializer.deserialize(decoder)
        }
    }

//    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
//        TODO("Not yet implemented")
//    }
//
//    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
//        TODO("Not yet implemented")
//    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }
}
