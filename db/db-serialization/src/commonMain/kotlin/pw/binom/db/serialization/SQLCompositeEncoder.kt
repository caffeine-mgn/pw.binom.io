package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.codes.SQLEncoder

interface SQLCompositeEncoder : CompositeEncoder {
    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder

    companion object {
        val NULL: SQLCompositeEncoder = object : SQLCompositeEncoder {
            @ExperimentalSerializationApi
            override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder = SQLEncoder.NULL

            override val serializersModule: SerializersModule
                get() = EmptySerializersModule()

            override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
            }

            override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
            }

            override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
            }

            override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
            }

            override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
            }

            override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
            }

            override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
            }

            @ExperimentalSerializationApi
            override fun <T : Any> encodeNullableSerializableElement(
                descriptor: SerialDescriptor,
                index: Int,
                serializer: SerializationStrategy<T>,
                value: T?
            ) {
            }

            override fun <T> encodeSerializableElement(
                descriptor: SerialDescriptor,
                index: Int,
                serializer: SerializationStrategy<T>,
                value: T
            ) {
            }

            override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
            }

            override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
            }

            override fun endStructure(descriptor: SerialDescriptor) {
            }
        }
    }
}
