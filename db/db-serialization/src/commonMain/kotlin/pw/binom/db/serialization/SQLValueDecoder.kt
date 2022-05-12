package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.ResultSet

class SQLValueDecoder(
    val classDescriptor: SerialDescriptor,
    val fieldIndex: Int,
    val columnPrefix: String?,
    val resultSet: ResultSet,
    override val serializersModule: SerializersModule,
) : Decoder {

    val columnName = (columnPrefix ?: "") + classDescriptor.getElementName(fieldIndex)

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val prefix = (columnPrefix ?: "") +
            (classDescriptor.getElementAnnotation<Embedded>(fieldIndex)?.prefix ?: "")

        return when {
            descriptor == ByteArraySerializer().descriptor -> ByteArraySQLCompositeDecoder(
                data = resultSet.getBlob(columnName)!!, serializersModule = serializersModule
            )
            else -> SQLCompositeDecoder(
                columnPrefix = prefix,
                resultSet = resultSet,
                serializersModule = serializersModule
            )
//            else -> throw SQLException("")
        }
    }

    override fun decodeBoolean(): Boolean =
        resultSet.getBoolean(columnName)!!

    override fun decodeByte(): Byte =
        resultSet.getInt(columnName)!!.toByte()

    override fun decodeChar(): Char =
        resultSet.getString(columnName)!![0]

    override fun decodeDouble(): Double =
        resultSet.getDouble(columnName)!!

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val byOrder = enumDescriptor.annotations.any { it is EnumOrderValue }
        val byName = enumDescriptor.annotations.any { it is EnumNameValue }
        if (byOrder && byName) {
            throw SerializationException("Invalid configuration of ${enumDescriptor.serialName}. Enum should use only one of @EnumOrderValue or @EnumNameValue")
        }
        val columnValue = resultSet.getString(columnName)!!
        val columnValueInt = columnValue.toIntOrNull()
        for (i in 0 until enumDescriptor.elementsCount) {
            val elementAnnotations = enumDescriptor.getElementAnnotations(i)
            if (columnValueInt != null) {
                val code = elementAnnotations.find { it is EnumCodeValue }?.let { it as EnumCodeValue }?.code
                if (columnValueInt == code) {
                    return i
                }
            }
            val alias = elementAnnotations.find { it is EnumAliasValue }?.let { it as EnumAliasValue }?.alias
            if (columnValue == alias) {
                return i
            }
        }
        throw SerializationException("Can't find enum ${enumDescriptor.serialName} by value \"$columnValue\"")
    }

    override fun decodeFloat(): Float =
        resultSet.getFloat(columnName)!!

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        TODO("Not yet implemented")
    }

    override fun decodeInt(): Int =
        resultSet.getInt(columnName)!!

    override fun decodeLong(): Long =
        resultSet.getLong(columnName)!!

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        TODO("Not yet implemented")
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    override fun decodeShort(): Short =
        resultSet.getInt(columnName)!!.toShort()

    override fun decodeString(): String =
        resultSet.getString(columnName)!!
}
