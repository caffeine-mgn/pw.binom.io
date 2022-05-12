package pw.binom.db.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.ResultSet

class SQLCompositeDecoder(
    val columnPrefix: String?,
    val resultSet: ResultSet,
    override val serializersModule: SerializersModule,
) : CompositeDecoder {

    private fun columnNotFound(name: String): Nothing =
        throw SerializationException("Column \"$name\" not found. Actual columns: ${resultSet.columns.map { "\"$it\"" }}")

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val column = (columnPrefix ?: "") + descriptor.getElementName(index)
        return resultSet.getBoolean(column) ?: columnNotFound(column)
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        resultSet.getInt((columnPrefix ?: "") + descriptor.getElementName(index))!!.toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        val txt = resultSet.getString((columnPrefix ?: "") + descriptor.getElementName(index))!!
        if (txt.length != 1) {
            throw SerializationException("Can't get char from text \"${txt}\"")
        }
        return txt[0]
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        resultSet.getDouble((columnPrefix ?: "") + descriptor.getElementName(index))!!

    var cursor = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (cursor == descriptor.elementsCount) {
            return CompositeDecoder.DECODE_DONE
        }
        for (it in cursor until descriptor.elementsCount) {
            if (descriptor.getElementDescriptor(it).kind is StructureKind) {
                return cursor++
            } else {
                val column = (columnPrefix ?: "") + descriptor.getElementName(it)
                if (resultSet.columns.indexOfFirst { it == column } != -1) {
                    return cursor++
                }
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        resultSet.getFloat((columnPrefix ?: "") + descriptor.getElementName(index))!!

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        TODO("Not yet implemented")
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        resultSet.getInt((columnPrefix ?: "") + descriptor.getElementName(index))!!

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        resultSet.getLong((columnPrefix ?: "") + descriptor.getElementName(index))!!

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val embedded = descriptor.getElementAnnotation<Embedded>(index)
        if (embedded != null) {
            val exist = (0 until descriptor.elementsCount).any { index ->
                val elementName = descriptor.getElementName(index)
                val el = "${columnPrefix ?: ""}${embedded.prefix}$elementName"
                resultSet.columns.any { it == el }
            }
            if (!exist) {
                return previousValue
            }
            val decoder = SQLValueDecoder(
                classDescriptor = descriptor,
                fieldIndex = index,
                columnPrefix = columnPrefix,
                resultSet = resultSet,
                serializersModule = serializersModule
            )
            return deserializer.deserialize(decoder)
        }
        return if (resultSet.isNull((columnPrefix ?: "") + descriptor.getElementName(index))) {
            previousValue
        } else {
            val decoder = SQLValueDecoder(
                classDescriptor = descriptor,
                fieldIndex = index,
                columnPrefix = columnPrefix,
                resultSet = resultSet,
                serializersModule = serializersModule
            )
            deserializer.deserialize(decoder)
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T =
        deserializer.deserialize(
            SQLValueDecoder(
                classDescriptor = descriptor,
                fieldIndex = index,
                serializersModule = serializersModule,
                resultSet = resultSet,
                columnPrefix = columnPrefix,
            )
        )

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        resultSet.getInt((columnPrefix ?: "") + descriptor.getElementName(index))!!.toShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        resultSet.getString((columnPrefix ?: "") + descriptor.getElementName(index))!!

    override fun endStructure(descriptor: SerialDescriptor) {
    }
}
