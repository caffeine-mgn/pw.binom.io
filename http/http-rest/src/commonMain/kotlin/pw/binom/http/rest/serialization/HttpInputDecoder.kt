package pw.binom.http.rest.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.http.rest.DecodeFunc
import pw.binom.http.rest.EndpointDescription
import pw.binom.io.http.HttpInput

@Suppress("UNCHECKED_CAST")
class HttpInputDecoder : Decoder, CompositeDecoder {
  override var serializersModule: SerializersModule = EmptySerializersModule()
  private var input: HttpInput? = null
  private var description: EndpointDescription<Any?>? = null
  private var pathVariables: Map<String, String> = emptyMap()
  private var queryVariables: Map<String, String?> = emptyMap()
  private var body: DecodeFunc<Any?, Any?>? = null
  private var data: Any? = null

  fun <INPUT, DATA> reset(
    input: HttpInput,
    description: EndpointDescription<INPUT>,
    data: DATA?,
    body: DecodeFunc<INPUT, DATA>?,
  ) {
    this.input = input
    this.body = body as DecodeFunc<Any?, Any?>?
    this.data = data
    this.description = description as EndpointDescription<Any?>
    queryVariables = input.query?.toMap() ?: emptyMap()
    pathVariables = input.path.getVariables(description.path) ?: emptyMap()
  }

  override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
    readString(index).toBoolean()

  override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
    readString(index).toByte()

  override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
    val str = readString(index)
    require(str.length == 1) { "Can't cast \"$str\" to char" }
    return str[0]
  }

  override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
    readString(index).toDouble()

  private var cursor = -1

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    val description = description!!
    while (true) {
      cursor++
      if (cursor >= descriptor.elementsCount) {
        return CompositeDecoder.DECODE_DONE
      }
      if (description.getParam[cursor] && description.nameByIndex[cursor] !in queryVariables) {
        continue
      }
      if (description.headerParam[cursor] && description.nameByIndex[cursor] !in input!!.inputHeaders) {
        continue
      }
      return cursor
    }
  }

  override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
    readString(index).toFloat()

  override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
    readString(index).toInt()

  override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
    readString(index).toLong()

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?,
  ): T? = decodeSerializableElement(
    descriptor = descriptor,
    index = index,
    deserializer = deserializer,
    previousValue = previousValue,
  )

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?,
  ): T {
    val description = description!!
    if (description.bodyIndex == index) {
      return body?.decode(ser = deserializer, data = data, input = input!!) as T
    }
    val decoder = StringDecoder(serializersModule)
    decoder.value = readString(index)
    return deserializer.deserialize(decoder)
  }

  override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
    readString(index).toShort()

  private fun readString(index: Int): String {
    val description = description!!
    if (description.getParam[index]) {
      return queryVariables[description.nameByIndex[index]]!!
    }
    if (description.headerParam[index]) {
      return input!!.inputHeaders[description.nameByIndex[index]]!!.first()
    }
    if (description.pathParam[index]) {
      return pathVariables[description.nameByIndex[index]]!!
    }
    TODO()
  }

  override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
    val description = description!!
    if (description.bodyIndex == index) {
      return body as String
    }
    return readString(index = index)
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    require(descriptor === description!!.serializer.descriptor)
    return this
  }

  override fun decodeBoolean(): Boolean {
    TODO("Not yet implemented")
  }

  override fun decodeByte(): Byte {
    TODO("Not yet implemented")
  }

  override fun decodeChar(): Char {
    TODO("Not yet implemented")
  }

  override fun decodeDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
    TODO("Not yet implemented")
  }

  override fun decodeFloat(): Float {
    TODO("Not yet implemented")
  }

  override fun decodeInline(descriptor: SerialDescriptor): Decoder {
    TODO("Not yet implemented")
  }

  override fun decodeInt(): Int {
    TODO("Not yet implemented")
  }

  override fun decodeLong(): Long {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean {
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing? {
    TODO("Not yet implemented")
  }

  override fun decodeShort(): Short {
    TODO("Not yet implemented")
  }

  override fun decodeString(): String {
    TODO("Not yet implemented")
  }
}
