package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

class XmlListEncoder(
    val store: XmlElement,
    val tagName: String,
    val ns: String?,
    override val serializersModule: SerializersModule
) : CompositeEncoder,
    Encoder {
    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        TODO("Not yet implemented")
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        TODO("Not yet implemented")
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        TODO("Not yet implemented")
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        TODO("Not yet implemented")
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
        if (descriptor.kind is StructureKind.LIST) {
            serializer.serialize(this, value)
        } else {
            TODO("Not yet implemented")
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        TODO("Not yet implemented")
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        TODO("Not yet implemented")
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return this
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
        val el = XmlElement(
            tag = tagName,
            nameSpace = ns
        )
        el.body = value
        el.parent = store
    }
}
