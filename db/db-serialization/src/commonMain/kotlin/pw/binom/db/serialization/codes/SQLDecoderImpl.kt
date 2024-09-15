@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.serialization.*
import pw.binom.uuid.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SQLDecoderImpl(val ctx: SQLDecoderPool, val onClose: (SQLDecoderImpl) -> Unit) : SQLDecoder {
  var name = ""
  var input: DataProvider = DataProvider.EMPTY
  override var serializersModule: SerializersModule = EmptySerializersModule()

  companion object {
    private fun findByAlias(
      value: String,
      enumDescriptor: SerialDescriptor,
    ): Int {
      for (i in 0 until enumDescriptor.elementsCount) {
        val alias = enumDescriptor.getElementAnnotation<Enumerate.Alias>(i) ?: continue
        alias.name.forEach {
          if (it == value) {
            return i
          }
        }
      }
      return CompositeDecoder.UNKNOWN_NAME
    }

    fun decodeEnum(
      name: String,
      input: DataProvider,
      enumDescriptor: SerialDescriptor,
    ): Int {
      val enumerateType =
        enumDescriptor.getElementAnnotation<Enumerate>()?.type
          ?: Enumerate.Type.BY_NAME
      when (enumerateType) {
        Enumerate.Type.BY_NAME -> {
          val enumName = input.getString(name)
          var index = enumDescriptor.getElementIndex(enumName)
          if (index == CompositeDecoder.UNKNOWN_NAME) {
            index = findByAlias(value = enumName, enumDescriptor = enumDescriptor)
          }
          if (index == CompositeDecoder.UNKNOWN_NAME) {
            throw SerializationException("Can't find \"$enumName\" in ${enumDescriptor.serialName}")
          }
          if (index < 0 || index >= enumDescriptor.elementsCount) {
            throw SerializationException("Can't decode ${enumDescriptor.serialName}: invalid index of element: $index")
          }
          return index
        }

        Enumerate.Type.BY_ORDER -> {
          val index = input.getInt(name)
          if (index < 0 || index >= enumDescriptor.elementsCount) {
            if (index == CompositeDecoder.UNKNOWN_NAME) {
              val indexByAlias = findByAlias(value = input.getString(name), enumDescriptor = enumDescriptor)
              if (indexByAlias != CompositeDecoder.UNKNOWN_NAME) {
                return indexByAlias
              }
            }
            throw SerializationException("Can't decode ${enumDescriptor.serialName}: invalid index of element: $index")
          }

          return index
        }
      }
    }
  }

  override fun decodeDateTime(): DateTime {
    val r = input.getDateTime(name)
    onClose(this)
    return r
  }

  override fun decodeDate(): Date {
    val r = input.getDate(name)
    onClose(this)
    return r
  }

  override fun decodeUUID(): UUID {
    val r = input.getUUID(name)
    onClose(this)
    return r
  }

  @OptIn(ExperimentalUuidApi::class)
  override fun decodeUuid(): Uuid {
    val r = input.getUuid(name)
    onClose(this)
    return r
  }

  override fun decodeByteArray(): ByteArray {
    val r = input.getByteArray(name)
    onClose(this)
    return r
  }

  override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeDecoder {
    if (descriptor === ByteArraySerializer().descriptor) {
      val value = input.getByteArray(name)
      val result =
        ctx.decodeByteArray(
          prefix = name,
          input = input,
          serializersModule = serializersModule,
          data = value,
        )
      onClose(this)
      return result
    }
    val decoder =
      ctx.decoderStruct(
        prefix = name,
        input = input,
        serializersModule = serializersModule,
      )
    onClose(this)
    return decoder
  }

  override fun decodeBoolean(): Boolean {
    val r = input.getBoolean(name)
    onClose(this)
    return r
  }

  override fun decodeByte(): Byte {
    val r = input.getByte(name)
    onClose(this)
    return r
  }

  override fun decodeChar(): Char {
    val r = input.getChar(name)
    onClose(this)
    return r
  }

  override fun decodeDouble(): Double {
    val r = input.getDouble(name)
    onClose(this)
    return r
  }

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
    try {
      decodeEnum(name = name, input = input, enumDescriptor = enumDescriptor)
    } finally {
      onClose(this)
    }

  override fun decodeFloat(): Float {
    val r = input.getFloat(name)
    onClose(this)
    return r
  }

  @ExperimentalSerializationApi
  override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
    onClose(this)
    TODO("Not yet implemented")
  }

  override fun decodeInt(): Int {
    val r = input.getInt(name)
    onClose(this)
    return r
  }

  override fun decodeLong(): Long {
    val r = input.getLong(name)
    onClose(this)
    return r
  }

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean {
    onClose(this)
    TODO("Not yet implemented")
  }

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing? {
    onClose(this)
    TODO("Not yet implemented")
  }

  override fun decodeShort(): Short {
    val r = input.getShort(name)
    onClose(this)
    return r
  }

  override fun decodeString(): String {
    val r = input.getString(name)
    onClose(this)
    return r
  }
}
