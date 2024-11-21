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
  ): Boolean = readValue(
    descriptor = descriptor,
    index = index
  ).toBoolean()

  override fun decodeByteElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Byte = readValue(
    descriptor = descriptor,
    index = index
  ).toByte()

  override fun decodeCharElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Char = readValue(
    descriptor = descriptor,
    index = index
  ).single()

  override fun decodeDoubleElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Double = readValue(
    descriptor = descriptor,
    index = index
  ).toDouble()

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
  ): Float = readValue(
    descriptor = descriptor,
    index = index
  ).toFloat()

  override fun decodeInlineElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Decoder = PropertiesDecoder(
    root = root[descriptor.getElementName(index)],
    serializersModule = serializersModule,
    prefix = "",
  )

  override fun decodeIntElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Int = readValue(
    descriptor = descriptor,
    index = index
  ).toInt()

  override fun decodeLongElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): Long = readValue(
    descriptor = descriptor,
    index = index
  ).toLong()

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?,
  ): T? {
    val el = root[descriptor.getElementName(index)] ?: return null
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
  ): Short = readValue(
    descriptor = descriptor,
    index = index
  ).toShort()

  private fun readValue(
    descriptor: SerialDescriptor,
    index: Int,
  ): String {
    val value = root[descriptor.getElementName(index)] as PropertyValue.Value
    return value.content!!
  }

  override fun decodeStringElement(
    descriptor: SerialDescriptor,
    index: Int,
  ): String = readValue(
    descriptor = descriptor,
    index = index
  )

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}
