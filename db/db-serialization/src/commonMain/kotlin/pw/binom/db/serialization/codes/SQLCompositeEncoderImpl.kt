package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.*

class SQLCompositeEncoderImpl(val ctx: SQLEncoderPool, val onClose: () -> Unit) : SQLCompositeEncoder {

    var prefix = ""
    var output: DateContainer = DateContainer.EMPTY

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder {
        val c = ctx.encodeValue(
            name = prefix + descriptor.getElementName(index),
            output = output,
            serializersModule = serializersModule,
        )
        return c
    }

    override var serializersModule: SerializersModule = EmptySerializersModule

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) {
            output[prefix + descriptor.getElementName(index)] = null
        } else {
            encodeSerializableElement(
                descriptor = descriptor,
                index = index,
                serializer = serializer,
                value = value
            )
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val splitter = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: ""
        val encoder = ctx.encodeValue(
            name = prefix + descriptor.getElementName(index) + splitter,
            output = output,
            serializersModule = serializersModule
        )
        serializer.serialize(
            encoder = encoder,
            value = value
        )
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        output[prefix + descriptor.getElementName(index)] = value
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        onClose()
    }
}
