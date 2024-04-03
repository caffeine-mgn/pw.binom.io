package pw.binom.properties.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.properties.PropertyValue

@OptIn(ExperimentalSerializationApi::class)
class PropertiesObjectDecoder(
  val root: PropertyValue.Object,
  override val serializersModule: SerializersModule,
) : CompositeDecoder {
  override fun decodeBooleanElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Boolean {
    TODO("Not yet implemented")
  }

  override fun decodeByteElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Byte {
    TODO("Not yet implemented")
  }

  override fun decodeCharElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Char {
    TODO("Not yet implemented")
  }

  override fun decodeDoubleElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Double {
    TODO("Not yet implemented")
  }

  var cursor = 0

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    while (true) {
      if (cursor >= descriptor.elementsCount) {
        return CompositeDecoder.DECODE_DONE
      }
      val name = descriptor.getElementName(cursor)
      if (name in root) {
        return cursor++
      } else {
        cursor++
      }
    }
  }

  override fun decodeFloatElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Float {
    TODO("Not yet implemented")
  }

  override fun decodeInlineElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeIntElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Int {
    TODO("Not yet implemented")
  }

  override fun decodeLongElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Long {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?,
  ): T? {
    val el = root[descriptor.getElementName(index)]?:return null
    val d =
      PropertiesDecoder(
        root = el,
        serializersModule = serializersModule,
        prefix = "",
      )
    return deserializer.deserialize(d)
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?,
  ): T {
    val d =
      PropertiesDecoder(
        root = root[descriptor.getElementName(index)],
        serializersModule = serializersModule,
        prefix = "",
      )
    return deserializer.deserialize(d)
  }

  override fun decodeShortElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Short {
    TODO("Not yet implemented")
  }

  override fun decodeStringElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): String {
    val value = root[descriptor.getElementName(index)] as PropertyValue.Value
    return value.content!!
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}
