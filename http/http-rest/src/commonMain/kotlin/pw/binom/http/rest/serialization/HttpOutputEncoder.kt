package pw.binom.http.rest.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.http.rest.EndpointDescription
import pw.binom.io.http.HashHeaders2

@Suppress("UNCHECKED_CAST")
class HttpOutputEncoder(
  override val serializersModule: SerializersModule = EmptySerializersModule(),
  private val endpointDescription: EndpointDescription<Any?>,
) : Encoder, CompositeEncoder {
  val pathParams = HashMap<String, String>()
  val getParams = HashMap<String, String>()
  val headerParams = HashHeaders2()
  var responseCode: Int = 0
    private set
  private var bodyObj: Any? = null
  private var bodySerializer: SerializationStrategy<Any>? = null

  interface Body {
    val body: Any
    val serializer: SerializationStrategy<Any>
  }

  private val internalBody = object : Body {
    override val body
      get() = bodyObj!!
    override val serializer
      get() = bodySerializer!!
  }
  val body: Body?
    get() = if (bodyObj == null) null else internalBody
  private var obj: SerialDescriptor? = null

  fun clear() {
    pathParams.clear()
    getParams.clear()
    headerParams.clear()
    bodyObj = null
    bodySerializer = null
    obj = null
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
    check(obj == null || obj === descriptor)
    return this
  }

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

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    TODO("Not yet implemented")
  }

  override fun encodeFloat(value: Float) {
    TODO("Not yet implemented")
  }

  override fun encodeInline(descriptor: SerialDescriptor): Encoder {
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

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Boolean.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Byte.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Char.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Double.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Float.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    TODO("Not yet implemented")
  }

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    if (index == endpointDescription.responseCodeIndex) {
      responseCode = value
      return
    }
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Int.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    if (index == endpointDescription.responseCodeIndex) {
      responseCode = value.toInt()
      return
    }
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Long.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?,
  ) {
    value ?: return
    encodeSerializableElement(
      descriptor = descriptor,
      index = index,
      serializer = serializer,
      value = value
    )
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T,
  ) {
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = serializer as SerializationStrategy<Any>
      return
    }
    val strEncoder = StringEncoder()
    serializer.serialize(strEncoder, value)
    val str = strEncoder.value
    stringParam(index = index, value = str)
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    if (index == endpointDescription.responseCodeIndex) {
      responseCode = value.toInt()
      return
    }
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = Short.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value.toString())
  }

  private fun stringParam(index: Int, value: String) {
    if (endpointDescription.pathParam[index]) {
      pathParams[endpointDescription.nameByIndex[index]!!] = value
    }
    if (endpointDescription.getParam[index]) {
      getParams[endpointDescription.nameByIndex[index]!!] = value
    }
    if (endpointDescription.headerParam[index]) {
      headerParams.add(endpointDescription.nameByIndex[index]!!, value)
    }
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    if (index == endpointDescription.responseCodeIndex) {
      responseCode = value.toInt()
      return
    }
    if (index == this.endpointDescription.bodyIndex) {
      bodyObj = value
      bodySerializer = String.serializer() as SerializationStrategy<Any>
      return
    }
    stringParam(index, value)
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}
