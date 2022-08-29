package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.UUID
import pw.binom.date.DateTime
import pw.binom.db.serialization.*

class SQLDecoderImpl(val ctx: SQLDecoderPool, val onClose: (SQLDecoderImpl) -> Unit) : SQLDecoder {

    var name = ""
    var input: DataProvider = DataProvider.EMPTY
    override var serializersModule: SerializersModule = EmptySerializersModule

    override fun decodeDateTime(): DateTime {
        val r = input.getDateTime(name)
        onClose(this)
        return r
    }

    override fun decodeUUID(): UUID {
        val r = input.getUUID(name)
        onClose(this)
        return r
    }

    override fun decodeByteArray(): ByteArray {
        val r = input.getByteArray(name)
        onClose(this)
        return r
    }

    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeDecoder {
        if (descriptor === ByteArraySerializer().descriptor) {
            val result = ctx.decodeByteArray(
                prefix = name,
                input = input,
                serializersModule = serializersModule,
                data = input[name] as ByteArray
            )
            onClose(this)
            return result
        }
        val decoder = ctx.decoderStruct(
            prefix = name,
            input = input,
            serializersModule = serializersModule
        )
        onClose(this)
        return decoder
    }

    override fun decodeBoolean(): Boolean {
        val r = input.getBoolean(name)
        onClose(this)
        return r
    }

    override fun decodeByte(): Byte {
        val r = input.getByte(name)
        onClose(this)
        return r
    }

    override fun decodeChar(): Char {
        val r = input.getChar(name)
        onClose(this)
        return r
    }

    override fun decodeDouble(): Double {
        val r = input.getDouble(name)
        onClose(this)
        return r
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val byOrder = enumDescriptor.annotations.any { it is EnumOrderValue }
        if (byOrder) {
            val orderIndex = input.getInt(name)
            if (orderIndex >= enumDescriptor.elementsCount || orderIndex < 0) {
                onClose(this)
                throw SerializationException("Invalid order $orderIndex of enum ${enumDescriptor.serialName}")
            }
            onClose(this)
            return orderIndex
        }
        val value = input[name]
        if (value is String) {
            val index = enumDescriptor.getElementIndex(value)
//            if (index == CompositeDecoder.UNKNOWN_NAME) {
//                throw SerializationException("Can't find enum value \"$value\" in enum ${enumDescriptor.serialName}")
//            }
            // seaching by name
            onClose(this)
            return index
        }
        val code = when (value) {
            is String -> {
                val code = value.toIntOrNull()
                if (code == null) {
                    onClose(this)
                    throw SerializationException("Can't cast \"$value\" to Int for enum ${enumDescriptor.serialName}")
                }
                code
            }

            is Int -> value
            is Long -> value.toInt()
            else -> {
                onClose(this)
                throw SerializationException("Can't cast \"$value\" to Int for enum ${enumDescriptor.serialName} ")
            }
        }
        for (i in 0 until enumDescriptor.elementsCount) {
            val enumCode = enumDescriptor.getElementAnnotation<EnumCodeValue>(i)?.code
            if (enumCode == code) {
                onClose(this)
                return i
            }
        }
        throw SerializationException("Can't find value \"$value\" for decode to enum ${enumDescriptor.serialName}")
    }

    override fun decodeFloat(): Float {
        val r = input.getFloat(name)
        onClose(this)
        return r
    }

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        onClose(this)
        TODO("Not yet implemented")
    }

    override fun decodeInt(): Int {
        val r = input.getInt(name)
        onClose(this)
        return r
    }

    override fun decodeLong(): Long {
        val r = input.getLong(name)
        onClose(this)
        return r
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        onClose(this)
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        onClose(this)
        TODO("Not yet implemented")
    }

    override fun decodeShort(): Short {
        val r = input.getShort(name)
        onClose(this)
        return r
    }

    override fun decodeString(): String {
        val r = input.getString(name)
        onClose(this)
        return r
    }
}
