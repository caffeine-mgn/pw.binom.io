@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.db.serialization.codes

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.SQLCompositeDecoder

class ByteArraySQLCompositeDecoder(val onClose: (ByteArraySQLCompositeDecoder) -> Unit) : SQLCompositeDecoder {
    override var serializersModule: SerializersModule = EmptySerializersModule()
        private set
    var data: ByteArray = ByteArray(0)
        private set

    fun reset(data: ByteArray, serializersModule: SerializersModule) {
        this.data = data
        this.serializersModule = serializersModule
        cursor = -1
    }

    private var cursor = -1

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = data[index]

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = throwNotSupported()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = throwNotSupported()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        cursor++
        if (cursor == data.size) {
            return CompositeDecoder.DECODE_DONE
        }
        return cursor
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = throwNotSupported()

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = throwNotSupported()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = throwNotSupported()

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

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = throwNotSupported()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = throwNotSupported()

    override fun endStructure(descriptor: SerialDescriptor) {
        onClose(this)
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLDecoder = throwNotSupported()

    private fun throwNotSupported(): Nothing = throw SerializationException("Not supported")
}
