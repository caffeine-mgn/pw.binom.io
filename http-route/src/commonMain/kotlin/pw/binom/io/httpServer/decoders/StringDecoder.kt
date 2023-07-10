package pw.binom.io.httpServer.decoders

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

class StringDecoder(override val serializersModule: SerializersModule = EmptySerializersModule()) : Decoder {
  var value: String = ""
  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    throw SerializationException("Can't parse \"$value\" to ${descriptor.serialName}. Struct decoding not supported")
  }

  override fun decodeBoolean(): Boolean = value == "true" || value == "t" || value == "1"

  override fun decodeByte(): Byte =
    value.toByteOrNull() ?: throw SerializationException("Can't parse \"$value\" to Byte")

  override fun decodeChar(): Char {
    if (value.length != 1) {
      throw SerializationException("Can't parse \"$value\" to Char")
    }
    return value[0]
  }

  override fun decodeDouble(): Double =
    value.toDoubleOrNull() ?: throw SerializationException("Can't parse \"$value\" to Double")

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(value)

  override fun decodeFloat(): Float =
    value.toFloatOrNull() ?: throw SerializationException("Can't parse \"$value\" to Float")

  @ExperimentalSerializationApi
  override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

  override fun decodeInt(): Int =
    value.toIntOrNull() ?: throw SerializationException("Can't parse \"$value\" to Int")

  override fun decodeLong(): Long =
    value.toLongOrNull() ?: throw SerializationException("Can't parse \"$value\" to Long")

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean = true

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing? = null

  override fun decodeShort(): Short =
    value.toShortOrNull() ?: throw SerializationException("Can't parse \"$value\" to Short")

  override fun decodeString(): String = value
}
