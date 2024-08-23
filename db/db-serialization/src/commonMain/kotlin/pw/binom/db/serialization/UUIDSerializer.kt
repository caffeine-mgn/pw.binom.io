package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.db.serialization.codes.SQLDecoder
import pw.binom.db.serialization.codes.SQLEncoder
import pw.binom.uuid.UUID

object UUIDSerializer : KSerializer<UUID> {
  override fun deserialize(decoder: Decoder): UUID {
    if (decoder !is SQLDecoder) {
      return UUID.fromString(decoder.decodeString())
    }
    return decoder.decodeUUID()
  }

  override val descriptor: SerialDescriptor
    get() = String.serializer().descriptor

  override fun serialize(encoder: Encoder, value: UUID) {
    if (encoder !is SQLEncoder) {
      encoder.encodeString(value.toString())
      return
    }
    encoder.encodeUUID(value)
  }
}
