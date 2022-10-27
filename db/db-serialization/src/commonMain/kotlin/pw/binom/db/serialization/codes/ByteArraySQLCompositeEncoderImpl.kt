package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.DateContainer
import pw.binom.db.serialization.SQLCompositeEncoder

class ByteArraySQLCompositeEncoderImpl(val onClose: (ByteArraySQLCompositeEncoderImpl) -> Unit) : SQLCompositeEncoder {
    var prefix = ""
    var output: DateContainer = DateContainer.EMPTY
    var useQuotes: Boolean = false
    var excludeGenerated: Boolean = false
    var body = ByteArray(0)
        private set

    private fun throwNotSupported(): Nothing = throw SerializationException("Not supported")
    override var serializersModule: SerializersModule = EmptySerializersModule()
        private set

    fun reset(size: Int, serializersModule: SerializersModule) {
        body = ByteArray(size)
        this.serializersModule = serializersModule
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        body[index] = value
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        output.set(
            key = prefix,
            value = body,
            useQuotes = useQuotes,
        )
        onClose(this)
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        throwNotSupported()
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

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder {
        throwNotSupported()
    }
}
