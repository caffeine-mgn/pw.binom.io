package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.UUID
import pw.binom.date.Date

class SQLValueEncoder(
    val classDescriptor: SerialDescriptor,
    val fieldIndex: Int,
    val columnPrefix: String?,
    val map: MutableMap<String, Any?>,
    override val serializersModule: SerializersModule,
) : SQLEncoder {

    val columnName = (columnPrefix ?: "") + classDescriptor.getElementName(fieldIndex)
    override fun encodeDate(date: Date) {
        map[columnName] = date
    }

    override fun encodeUUID(uuid: UUID) {
        map[columnName] = uuid
    }

    override fun encodeByteArray(array: ByteArray) {
        map[columnName] = array
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return when {
            descriptor == ByteArraySerializer().descriptor -> ByteArraySQLCollectionCompositeEncoder(
                map = map,
                columnName = columnName,
                serializersModule = serializersModule,
                collectionSize = collectionSize,
            )
            else -> throw SerializationException("Not supported collection ${descriptor.serialName}")
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        TODO("Not yet implemented")
    }

    override fun encodeBoolean(value: Boolean) {
        map[columnName] = value
    }

    override fun encodeByte(value: Byte) {
        map[columnName] = value.toInt()
    }

    override fun encodeChar(value: Char) {
        map[columnName] = value.toString()
    }

    override fun encodeDouble(value: Double) {
        map[columnName] = value
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val code =
            enumDescriptor.getElementAnnotations(index).find { it is EnumCodeValue }?.let { it as EnumCodeValue }?.code
        val alias = enumDescriptor.getElementAnnotations(index).find { it is EnumAliasValue }
            ?.let { it as EnumAliasValue }?.alias
        val byOrder = enumDescriptor.annotations.any { it is EnumOrderValue }
        val byName = enumDescriptor.annotations.any { it is EnumNameValue }
        if (byOrder && byName) {
            throw SerializationException("Invalid configuration of ${enumDescriptor.serialName}. Enum should use only one of @EnumOrderValue or @EnumNameValue")
        }
        if (code != null && alias != null) {
            throw SerializationException(
                "Invalid configuration of ${enumDescriptor.serialName}.${
                enumDescriptor.getElementName(
                    index
                )
                } Enum should use only one of @EnumCodeValue or @EnumAliasValue"
            )
        }
        val value: Any = when {
            code != null -> code
            alias != null -> alias
            enumDescriptor.annotations.any { it is EnumOrderValue } -> index
            enumDescriptor.annotations.any { it is EnumNameValue } -> enumDescriptor.getElementName(index)
            else -> throw SerializationException("Can't detect valid enum ${enumDescriptor.serialName} serialization method")
        }
        map[columnName] = value
    }

    override fun encodeFloat(value: Float) {
        map[columnName] = value
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder {
        TODO("Not yet implemented")
    }

    override fun encodeInt(value: Int) {
        map[columnName] = value
    }

    override fun encodeLong(value: Long) {
        map[columnName] = value
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        map[columnName] = null
    }

    override fun encodeShort(value: Short) {
        map[columnName] = value.toInt()
    }

    override fun encodeString(value: String) {
        map[columnName] = value
    }
}
