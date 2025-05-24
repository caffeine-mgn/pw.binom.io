package pw.binom.strong.serialization.providers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import pw.binom.strong.serialization.SerializationProvider

open class ProtoBufProvider : SerializationProvider {
  override val mimeTypes: Collection<String> = listOf("application/protobuf")

  protected open var serialization: ProtoBuf = ProtoBuf

  override fun init(module: SerializersModule) {
    serialization = ProtoBuf(from = serialization) {
      this.serializersModule = module
    }
  }

  override fun <T> encode(serializer: KSerializer<T>, value: T): ByteArray =
    serialization.encodeToByteArray(serializer, value)

  override fun <T> decode(serializer: KSerializer<T>, data: ByteArray): T =
    serialization.decodeFromByteArray(serializer, data)
}
