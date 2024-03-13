package pw.binom.properties.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.properties.PropertyValue
import pw.binom.properties.serialization.annotations.PropertiesPrefix

class PropertiesDecoder(
  val root: PropertyValue?,
  override val serializersModule: SerializersModule,
  val prefix: String,
) : Decoder {
  companion object {
    fun <T> parse(
      serializer: DeserializationStrategy<T>,
      root: PropertyValue?,
      serializersModule: SerializersModule = EmptySerializersModule(),
      prefix: String = "",
    ) = serializer.deserialize(
      PropertiesDecoder(
        root = root,
        serializersModule = serializersModule,
        prefix = prefix,
      ),
    )
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    if (root == null) {
      throw SerializationException("Value missing")
    }
    val prefix1 = descriptor.annotations.find { it is PropertiesPrefix } as PropertiesPrefix?
    val p = prefix1?.prefix ?: ""
    val totalPrefix =
      if (p.isNotEmpty()) {
        if (prefix.isNotEmpty()) {
          "$prefix.$p"
        } else {
          p
        }
      } else {
        prefix
      }
    val vv = root.getByPath(totalPrefix)
    if (vv is PropertyValue.Enumerate) {
      return PropertiesListDecoder(
        root = vv,
        serializersModule = serializersModule,
      )
    }
    if (vv !is PropertyValue.Object) {
      return PropertiesObjectDecoder(
        root = PropertyValue.Object.EMPTY,
        serializersModule = serializersModule,
      )
    }
    return PropertiesObjectDecoder(
      root = vv,
      serializersModule = serializersModule,
    )
  }

  override fun decodeBoolean(): Boolean {
    val str = decodeString()
    if (str.isEmpty()) {
      throw SerializationException("Can't decode empty string to Boolean")
    }
    if (str == "true" || str == "t") {
      return true
    }
    if (str == "false" || str == "f") {
      return false
    }
    val int = str.toIntOrNull()
    if (int != null) {
      return int > 0
    }
    throw SerializationException("Can't decode \"$str\" to Boolean")
  }

  override fun decodeByte(): Byte = decodeString().toByte()

  override fun decodeChar(): Char {
    val str = decodeString()
    if (str.isEmpty()) {
      throw SerializationException("Can't decode empty string to char")
    }
    if (str.length != 1) {
      throw SerializationException("Can't decode \"$str\" to char")
    }
    return str[0]
  }

  override fun decodeDouble(): Double = decodeString().toDouble()

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(decodeString())

  override fun decodeFloat(): Float = decodeString().toFloat()

  override fun decodeInline(descriptor: SerialDescriptor): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeInt(): Int = decodeString().toInt()

  override fun decodeLong(): Long = decodeString().toLong()

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean = root != null

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing? {
    return null
  }

  override fun decodeShort(): Short = decodeString().toShort()

  override fun decodeString(): String = (root as PropertyValue.Value).content!!
}
