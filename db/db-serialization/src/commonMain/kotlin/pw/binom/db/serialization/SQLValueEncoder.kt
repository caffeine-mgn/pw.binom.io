package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.DateTime
import pw.binom.db.serialization.codes.SQLEncoder
import pw.binom.uuid.UUID

class SQLValueEncoder(
    val classDescriptor: SerialDescriptor,
    val fieldIndex: Int,
    val columnPrefix: String?,
    val map: MutableMap<String, Any?>,
    override val serializersModule: SerializersModule,
) : SQLEncoder {

    val columnName = (columnPrefix ?: "") + classDescriptor.getElementName(fieldIndex)
    override fun encodeDate(dateTime: DateTime) {
        map[columnName] = dateTime
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

    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder {
        val embedded = classDescriptor.getElementAnnotation<Embedded>(fieldIndex) ?: TODO()
        val embedded2 = classDescriptor.getElementAnnotation<EmbeddedSplitter>(fieldIndex)
        return SQLCompositeEncoderImpl2(
            columnPrefix = (columnPrefix ?: "") + classDescriptor.getElementName(fieldIndex) + (
                embedded2?.splitter
                    ?: "_"
                ),
            map = map,
            serializersModule = serializersModule,
        )
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
      val type = enumDescriptor.getElementAnnotation<Enumerate>()?.type ?: Enumerate.Type.BY_NAME
      map[columnName] = when (type) {
        Enumerate.Type.BY_NAME -> enumDescriptor.getElementName(index)
        Enumerate.Type.BY_ORDER -> index
      }
    }

    override fun encodeFloat(value: Float) {
        map[columnName] = value
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder {
        TODO("Not yet implemented. inlineDescriptor: ${inlineDescriptor.serialName}")
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
