package pw.binom.http.rest.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

class StringEncoder(override val serializersModule: SerializersModule = EmptySerializersModule()) : Encoder {
    var value = ""
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        throw SerializationException("Can't encode ${descriptor.serialName}. Struct decoding not supported")
    }

    override fun encodeBoolean(value: Boolean) {
        this.value = if (value) {
            "true"
        } else {
            "false"
        }
    }

    override fun encodeByte(value: Byte) {
        this.value = value.toString()
    }

    override fun encodeChar(value: Char) {
        this.value = value.toString()
    }

    override fun encodeDouble(value: Double) {
        this.value = value.toString()
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        this.value = enumDescriptor.getElementName(index)
    }

    override fun encodeFloat(value: Float) {
        this.value = value.toString()
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun encodeInt(value: Int) {
        this.value = value.toString()
    }

    override fun encodeLong(value: Long) {
        this.value = value.toString()
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
    }

    override fun encodeShort(value: Short) {
        this.value = value.toString()
    }

    override fun encodeString(value: String) {
        this.value = value
    }
}
