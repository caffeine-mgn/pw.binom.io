package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.UUID
import pw.binom.date.DateTime
import pw.binom.db.serialization.*

class SQLEncoderImpl(val ctx: SQLEncoderPool, val onClose: () -> Unit) : SQLEncoder {
    var name = ""
    override var serializersModule: SerializersModule = EmptySerializersModule
    var output: DateContainer = DateContainer.EMPTY
    override fun encodeDate(dateTime: DateTime) {
        output[name] = dateTime
        onClose()
    }

    override fun encodeUUID(uuid: UUID) {
        output[name] = uuid
        onClose()
    }

    override fun encodeByteArray(array: ByteArray) {
        output[name] = array
        onClose()
    }

    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder {
        val c = ctx.encodeStruct(
            prefix = name,
            output = output,
            serializersModule = serializersModule,
        )
        onClose()
        return c
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        when {
            ByteArraySerializer().descriptor === descriptor -> return ctx.encodeByteArray(
                size = collectionSize,
                prefix = name,
                output = output,
                serializersModule = serializersModule
            )
        }
        return super.beginCollection(descriptor, collectionSize)
    }

    override fun encodeBoolean(value: Boolean) {
        output[name] = value
        onClose()
    }

    override fun encodeByte(value: Byte) {
        output[name] = value
        onClose()
    }

    override fun encodeChar(value: Char) {
        output[name] = value
        onClose()
    }

    override fun encodeDouble(value: Double) {
        output[name] = value
        onClose()
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val code = enumDescriptor.getElementAnnotation<EnumCodeValue>(index)?.code
        val byOrder = enumDescriptor.annotations.any { it is EnumOrderValue }
        if (byOrder && code != null) {
            onClose()
            throw IllegalArgumentException("Invalid enum config: used both @EnumCodeValue and @EnumOrderValue")
        }
        if (byOrder) {
            encodeInt(index)
        } else {
            if (code != null) {
                encodeInt(code)
            } else {
                encodeString(enumDescriptor.getElementName(index))
            }
        }
    }

    override fun encodeFloat(value: Float) {
        output[name] = value
        onClose()
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder = this

    override fun encodeInt(value: Int) {
        output[name] = value
        onClose()
    }

    override fun encodeLong(value: Long) {
        output[name] = value
        onClose()
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        output[name] = null
        onClose()
    }

    override fun encodeShort(value: Short) {
        output[name] = value
        onClose()
    }

    override fun encodeString(value: String) {
        output[name] = value
        onClose()
    }
}
