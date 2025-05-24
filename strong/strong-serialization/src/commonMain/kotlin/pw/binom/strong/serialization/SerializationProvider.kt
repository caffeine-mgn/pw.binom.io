package pw.binom.strong.serialization

import kotlinx.serialization.KSerializer

interface SerializationProvider {
  companion object;
  val mimeTypes: Collection<String>
  fun <T> encode(serializer: KSerializer<T>, value: T): ByteArray
  fun <T> decode(serializer: KSerializer<T>, data: ByteArray): T
}
