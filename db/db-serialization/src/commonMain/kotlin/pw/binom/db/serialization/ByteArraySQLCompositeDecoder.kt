package pw.binom.db.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.codes.SQLDecoder

class ByteArraySQLCompositeDecoder(val data: ByteArray, override val serializersModule: SerializersModule) :
    SQLCompositeDecoder {
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        throwNotSupported()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        data[index]

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        throwNotSupported()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        throwNotSupported()

    private var cursor = -1
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        cursor++
        if (cursor == data.size) {
            return DECODE_DONE
        }
        return cursor
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        throwNotSupported()

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLDecoder =
        throwNotSupported()

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        throwNotSupported()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        throwNotSupported()

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? = throwNotSupported()

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = throwNotSupported()

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        throwNotSupported()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        throwNotSupported()

    override fun endStructure(descriptor: SerialDescriptor) {
    }

    private fun throwNotSupported(): Nothing = throw SerializationException("Not supported")
}
