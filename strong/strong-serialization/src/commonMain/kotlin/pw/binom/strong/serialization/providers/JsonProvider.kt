package pw.binom.strong.serialization.providers

import kotlinx.serialization.KSerializer
import pw.binom.strong.serialization.SerializationProvider

class JsonProvider: SerializationProvider {
  override val mimeTypes: Collection<String> =
    listOf("application/json")

  override fun <T> encode(serializer: KSerializer<T>, value: T): ByteArray {
    TODO("Not yet implemented")
  }

  override fun <T> decode(serializer: KSerializer<T>, data: ByteArray): T {
    TODO("Not yet implemented")
  }
}
