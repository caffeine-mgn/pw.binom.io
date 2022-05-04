package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class ByteArraySQLCollectionCompositeEncoder(
    val map: MutableMap<String, Any?>,
    val columnName: String,
    collectionSize: Int,
    override val serializersModule: SerializersModule
) : CompositeEncoder {
    private val resultList = ByteArray(collectionSize)
    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        throwNotSupported()
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        resultList[index] = value
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        throwNotSupported()
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        throwNotSupported()
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        throwNotSupported()
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        throwNotSupported()
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        throwNotSupported()
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        throwNotSupported()
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        throwNotSupported()
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        throwNotSupported()
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        throwNotSupported()
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        throwNotSupported()
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        map[columnName] = resultList
    }

    private fun throwNotSupported(): Nothing = throw SerializationException("Not supported")
}
