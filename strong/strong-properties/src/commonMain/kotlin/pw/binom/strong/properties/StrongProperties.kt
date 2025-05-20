package pw.binom.strong.properties

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

interface StrongProperties {
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> parse(serializer: KSerializer<T>, serializersModule: SerializersModule = EmptySerializersModule()): T
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> StrongProperties.parse(
  serializersModule: SerializersModule = EmptySerializersModule(),
) = parse(
  serializer = T::class.serializer(),
  serializersModule = serializersModule,
)
