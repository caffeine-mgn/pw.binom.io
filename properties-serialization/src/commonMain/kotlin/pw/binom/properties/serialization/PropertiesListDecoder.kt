package pw.binom.properties.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.properties.PropertyValue

class PropertiesListDecoder(
  val root: PropertyValue.Enumerate,
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

  private var cursor = 0

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    if (cursor >= root.size) {
      return CompositeDecoder.DECODE_DONE
    }
    return cursor++
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
    TODO("Not yet implemented")
  }

  override fun decodeSequentially(): Boolean {
    return true
  }

  override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
    return root.size
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?,
  ): T {
    val e = root[index] // ?: return previousValue!!
    val dd =
      PropertiesDecoder(
        root = e,
        prefix = "",
        serializersModule = serializersModule,
      )
    return deserializer.deserialize(dd)
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
    TODO("Not yet implemented")
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}
