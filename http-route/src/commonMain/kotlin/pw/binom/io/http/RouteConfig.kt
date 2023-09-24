package pw.binom.io.http

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

interface RouteConfig {
  val serializersModule: SerializersModule
    get() = EmptySerializersModule()

  suspend fun <T : Any> encodeValue(serializer: KSerializer<T>, value: T): String
  suspend fun <T : Any> decodeValue(serializer: KSerializer<T>, value: String): T
  suspend fun <T> decodeBody(serializer: KSerializer<T>, input: HttpInput): T
  suspend fun <T> encodeBody(serializer: KSerializer<T>, value: T, output: HttpOutput)
}
