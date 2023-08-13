package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.DateTime
import pw.binom.db.ResultSet
import pw.binom.db.serialization.codes.SQLDecoder
import pw.binom.uuid.UUID

class SQLValueDecoder(
  val classDescriptor: SerialDescriptor,
  val fieldIndex: Int,
  val columnPrefix: String?,
  val resultSet: ResultSet,
  override val serializersModule: SerializersModule,
) : SQLDecoder {

  val columnName = (columnPrefix ?: "") + classDescriptor.getElementName(fieldIndex)
  override fun decodeDateTime(): DateTime = resultSet.getDate(columnName)!!
  override fun decodeUUID(): UUID = resultSet.getUUID(columnName)!!
  override fun decodeByteArray(): ByteArray = resultSet.getBlob(columnName)!!

  override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeDecoder {
    val prefix = columnName +
      (classDescriptor.getElementAnnotation<EmbeddedSplitter>(fieldIndex)?.splitter ?: "_")

    return when {
      descriptor == ByteArraySerializer().descriptor -> ByteArraySQLCompositeDecoder(
        data = resultSet.getBlob(columnName)!!,
        serializersModule = serializersModule
      )

      else -> SQLCompositeDecoder2(
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
    return pw.binom.db.serialization.codes.SQLDecoderImpl.decodeEnum(
      name = columnName,
      input = ResultSetDataProvider(resultSet),
      enumDescriptor = enumDescriptor
    )
    /*
      val byOrder = enumDescriptor.annotations.any { it is Enumerate }
      val columnValue = resultSet.getString(columnName)!!
      val columnValueInt = columnValue.toIntOrNull()

      if (byOrder) {
          val i = columnValueInt ?: throw SerializationException("Can't cast \"$columnValue\" to int")
          if (i < 0 || i >= enumDescriptor.elementsCount) {
              throw SerializationException("Illegal enum order value: $i")
          }
          return i
      }

      for (i in 0 until enumDescriptor.elementsCount) {
          if (columnValueInt != null) {
              val code = enumDescriptor.getElementAnnotation<EnumCodeValue>(i)?.code
              if (columnValueInt == code) {
                  return i
              }
          }
          if (columnValue == enumDescriptor.getElementName(i)) {
              return i
          }
      }
      throw SerializationException("Can't find enum ${enumDescriptor.serialName} by value \"$columnValue\"")
    */
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
