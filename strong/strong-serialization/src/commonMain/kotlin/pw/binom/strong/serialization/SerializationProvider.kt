package pw.binom.strong.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

interface SerializationProvider {
  companion object;
  fun init(module: SerializersModule) {}
  val mimeTypes: Collection<String>
  fun <T> encode(serializer: KSerializer<T>, value: T): ByteArray
  fun <T> decode(serializer: KSerializer<T>, data: ByteArray): T
}
