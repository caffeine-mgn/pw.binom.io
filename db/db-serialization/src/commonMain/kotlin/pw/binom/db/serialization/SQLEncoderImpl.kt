package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class SQLEncoderImpl(
    val columnPrefix: String?,
    val map: MutableMap<String, Any?>,
    override val serializersModule: SerializersModule
) : Encoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
        SQLCompositeEncoder(columnPrefix = columnPrefix, map = map, serializersModule = serializersModule)

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

    @OptIn(ExperimentalSerializationApi::class)
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val value = when {
            enumDescriptor.annotations.any { it is EnumOrderValue } -> index
            enumDescriptor.annotations.any { it is EnumNameValue } -> enumDescriptor.getElementName(index)
            else -> {
                val code = enumDescriptor.getElementAnnotations(index).find { it is EnumCodeValue }
                    ?.let { it as EnumCodeValue }?.code
                    ?: throw SerializationException("Can't detect valid enum ${enumDescriptor.serialName} serialization method")
                code
            }
        }
        map[(columnPrefix ?: "") + enumDescriptor.getElementName(index)] = value
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
        TODO("Not yet implemented")
    }
}
