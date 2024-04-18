package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.codes.SQLDecoder

interface SQLDecoderPool {
  fun decoderValue(
    name: String,
    input: DataProvider,
    serializersModule: SerializersModule,
  ): SQLDecoder

  fun decoderStruct(
    prefix: String,
    input: DataProvider,
    serializersModule: SerializersModule,
  ): SQLCompositeDecoder

  fun decodeByteArray(
    prefix: String,
    input: DataProvider,
    serializersModule: SerializersModule,
    data: ByteArray,
  ): SQLCompositeDecoder

  fun <T> decode(
    serializer: KSerializer<T>,
    name: String,
    input: DataProvider,
    serializersModule: SerializersModule = EmptySerializersModule(),
  ): T {
    val decoder =
      decoderValue(
        name = name,
        input = input,
        serializersModule = serializersModule,
      )
    return serializer.deserialize(decoder)
  }
}
