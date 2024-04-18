package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.codes.SQLEncoder

interface SQLEncoderPool {
  fun encodeValue(
    name: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLEncoder

  fun encodeStruct(
    prefix: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLCompositeEncoder

  fun encodeByteArray(
    size: Int,
    prefix: String,
    output: DateContainer,
    serializersModule: SerializersModule,
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ): SQLCompositeEncoder

  fun <T> encode(
    serializer: KSerializer<T>,
    value: T,
    name: String,
    output: DateContainer,
    serializersModule: SerializersModule = EmptySerializersModule(),
    useQuotes: Boolean,
    excludeGenerated: Boolean,
  ) {
    val encoder =
      encodeValue(
        name = name,
        output = output,
        serializersModule = serializersModule,
        useQuotes = useQuotes,
        excludeGenerated = excludeGenerated,
      )
    serializer.serialize(encoder, value)
  }
}
