package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.serialization.SQLCompositeEncoder
import pw.binom.uuid.UUID

interface SQLEncoder : Encoder {
  fun encodeDate(dateTime: DateTime)

  fun encodeDate(dateTime: Date)

  fun encodeUUID(uuid: UUID)

  fun encodeByteArray(array: ByteArray)

  override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder

  @ExperimentalSerializationApi
  override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder

  companion object {
    val NULL: SQLEncoder =
      object : SQLEncoder {
        override fun encodeDate(dateTime: DateTime) {
        }

        override fun encodeDate(dateTime: Date) {
        }

        override fun encodeUUID(uuid: UUID) {
        }

        override fun encodeByteArray(array: ByteArray) {
        }

        override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder = SQLCompositeEncoder.NULL

        @ExperimentalSerializationApi
        override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder = this

        override val serializersModule: SerializersModule
          get() = EmptySerializersModule()

        override fun encodeBoolean(value: Boolean) {
        }

        override fun encodeByte(value: Byte) {
        }

        override fun encodeChar(value: Char) {
        }

        override fun encodeDouble(value: Double) {
        }

        override fun encodeEnum(
          enumDescriptor: SerialDescriptor,
          index: Int,
        ) {
        }

        override fun encodeFloat(value: Float) {
        }

        override fun encodeInt(value: Int) {
        }

        override fun encodeLong(value: Long) {
        }

        @ExperimentalSerializationApi
        override fun encodeNull() {
        }

        override fun encodeShort(value: Short) {
        }

        override fun encodeString(value: String) {
        }
      }
  }
}
