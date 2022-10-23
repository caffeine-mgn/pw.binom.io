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
    var useQuotes: Boolean = false

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder {
        val c = ctx.encodeValue(
            name = prefix + descriptor.getElementName(index),
            output = output,
            serializersModule = serializersModule,
            useQuotes = descriptor.isUseQuotes(index) || useQuotes
        )
        return c
    }

    override var serializersModule: SerializersModule = EmptySerializersModule()

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) {
            output.set(
                key = prefix + descriptor.getElementName(index),
                value = null,
                useQuotes = descriptor.isUseQuotes(index) || useQuotes
            )
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
            serializersModule = serializersModule,
            useQuotes = descriptor.isUseQuotes(index) || useQuotes
        )
        serializer.serialize(
            encoder = encoder,
            value = value
        )
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        output.set(
            key = prefix + descriptor.getElementName(index),
            useQuotes = descriptor.isUseQuotes(index) || useQuotes,
            value = value,
        )
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        onClose()
    }
}
