package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class SQLEncoderImpl(
  val columnPrefix: String?,
  val map: MutableMap<String, Any?>,
  override val serializersModule: SerializersModule,
) : Encoder {
  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
    SQLCompositeEncoderImpl2(columnPrefix = columnPrefix, map = map, serializersModule = serializersModule)

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
    val type = enumDescriptor.getElementAnnotation<Enumerate>()?.type ?: Enumerate.Type.BY_NAME
    map[(columnPrefix ?: "") + enumDescriptor.getElementName(index)] = when (type) {
      Enumerate.Type.BY_NAME -> enumDescriptor.getElementName(index)
      Enumerate.Type.BY_ORDER -> index
    }
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
