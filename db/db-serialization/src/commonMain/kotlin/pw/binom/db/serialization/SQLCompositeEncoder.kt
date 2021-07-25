package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class SQLCompositeEncoder(
    val columnPrefix: String?,
    val map: MutableMap<String, Any?>,
    override val serializersModule: SerializersModule
) :
    CompositeEncoder {
    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value.toInt()
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value.toString()
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) {
            map[(columnPrefix ?: "") + descriptor.getElementName(index)] = null
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
        serializer.serialize(
            encoder = SQLValueEncoder(
                classDescriptor = descriptor,
                fieldIndex = index,
                columnPrefix = columnPrefix,
                map = map,
                serializersModule = serializersModule,
            ),
            value = value,
        )
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value.toInt()
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        map[(columnPrefix ?: "") + descriptor.getElementName(index)] = value
    }

    override fun endStructure(descriptor: SerialDescriptor) {

    }
}