package pw.binom.properties.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.properties.PropertyValue

@OptIn(ExperimentalSerializationApi::class)
class PropertiesMapDecoder(
  val root: PropertyValue.Object,
  override val serializersModule: SerializersModule,
) : CompositeDecoder {
  val iterator = root.names.iterator()
  val list = root.names
    .map {
      it to root[it]
    }

  override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
    TODO("Not yet implemented")
  }

  override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
    TODO("Not yet implemented")
  }

  override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
    TODO("Not yet implemented")
  }

  override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
    TODO("Not yet implemented")
  }

  private var key = true
  private var nameValue = ""
  private var cursor = -1
  private var eof = false
  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    if (key) {
      if (!iterator.hasNext()) {
        eof = true
        return CompositeDecoder.DECODE_DONE
      }
      key = false
      cursor++
      nameValue = iterator.next()
      return cursor
    } else {
      if (eof) {
        return CompositeDecoder.DECODE_DONE
      }
      cursor++
      key = true
      return cursor
    }
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?,
  ): T {
    val isKey = index % 2 == 0
    val listIndex = index / 2
    val key = list[listIndex].first
    val p = if (isKey) {
      PropertyValue.Value.of(key)
    } else {
      root[key]
    }
    val d =
      PropertiesDecoder(
        root = p,
        serializersModule = serializersModule,
        prefix = "",
      )
    return deserializer.deserialize(d)
  }

  override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
    TODO("Not yet implemented")
  }

  override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
    TODO("Not yet implemented")
  }

  override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
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

  override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
    TODO("Not yet implemented")
  }

  override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
    TODO("Not yet implemented")
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}
