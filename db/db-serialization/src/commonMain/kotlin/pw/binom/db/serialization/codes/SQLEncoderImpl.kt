package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.DateTime
import pw.binom.db.serialization.*
import pw.binom.uuid.UUID

class SQLEncoderImpl(val ctx: SQLEncoderPool, val onClose: () -> Unit) : SQLEncoder {
    var name = ""
    var useQuotes: Boolean = false
    override var serializersModule: SerializersModule = EmptySerializersModule
    var output: DateContainer = DateContainer.EMPTY
    var excludeGenerated: Boolean = false
    override fun encodeDate(dateTime: DateTime) {
        output.set(
            key = name,
            value = dateTime,
            useQuotes = useQuotes
        )
        onClose()
    }

    override fun encodeUUID(uuid: UUID) {
        output.set(
            key = name,
            value = uuid,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeByteArray(array: ByteArray) {
        output.set(
            key = name,
            value = array,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder {
        val c = ctx.encodeStruct(
            prefix = name,
            output = output,
            serializersModule = serializersModule,
            useQuotes = useQuotes,
            excludeGenerated = excludeGenerated,
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
                serializersModule = serializersModule,
                useQuotes = useQuotes,
                excludeGenerated = excludeGenerated,
            )
        }
        return super.beginCollection(descriptor, collectionSize)
    }

    override fun encodeBoolean(value: Boolean) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeByte(value: Byte) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeChar(value: Char) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeDouble(value: Double) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
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
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder = this

    override fun encodeInt(value: Int) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeLong(value: Long) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        output.set(
            key = name,
            value = null,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeShort(value: Short) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }

    override fun encodeString(value: String) {
        output.set(
            key = name,
            value = value,
            useQuotes = useQuotes,
        )
        onClose()
    }
}
