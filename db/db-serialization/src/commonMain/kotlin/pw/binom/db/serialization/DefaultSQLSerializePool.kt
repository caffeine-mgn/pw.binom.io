package pw.binom.db.serialization

import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.codes.*
import pw.binom.db.serialization.codes.ByteArraySQLCompositeDecoder
import pw.binom.db.serialization.codes.SQLDecoderImpl
import pw.binom.db.serialization.codes.SQLEncoderImpl

object DefaultSQLSerializePool : SQLEncoderPool, SQLDecoderPool {
  override fun encodeValue(
    name: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLEncoder {
    val c = SQLEncoderImpl(this) {}
    c.name = name
    c.useQuotes = useQuotes
    c.serializersModule = serializersModule
    c.excludeGenerated = excludeGenerated
    c.output = output
    return c
  }

  override fun encodeStruct(
    prefix: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLCompositeEncoder {
    val c = SQLCompositeEncoderImpl(this) {}
    c.prefix = prefix
    c.output = output
    c.useQuotes = useQuotes
    c.serializersModule = serializersModule
    c.excludeGenerated = excludeGenerated
    return c
  }

  override fun encodeByteArray(
    size: Int,
    prefix: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLCompositeEncoder {
    val c = ByteArraySQLCompositeEncoderImpl {}
    c.prefix = prefix
    c.output = output
    c.useQuotes = useQuotes
    c.excludeGenerated = excludeGenerated
    c.reset(
      size = size,
      serializersModule = serializersModule,
    )
    return c
  }

  override fun decoderValue(
    name: String,
    input: DataProvider,
    serializersModule: SerializersModule,
  ): SQLDecoder {
    val c = SQLDecoderImpl(this) {}
    c.name = name
    c.input = input
    c.serializersModule = serializersModule
    return c
  }

  override fun decoderStruct(
    prefix: String,
    input: DataProvider,
    serializersModule: SerializersModule,
  ): SQLCompositeDecoder {
    val c = SQLCompositeDecoderImpl(this) {}
    c.input = input
    c.serializersModule = serializersModule
    c.prefix = prefix
    return c
  }

  override fun decodeByteArray(
    prefix: String,
    input: DataProvider,
    serializersModule: SerializersModule,
    data: ByteArray,
  ): SQLCompositeDecoder {
    val c = ByteArraySQLCompositeDecoder {}
    c.reset(
      data = data,
      serializersModule = serializersModule,
    )
    return c
  }
}
