package pw.binom.mq.nats.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.jsonPrimitive
import pw.binom.io.socket.DomainSocketAddress

internal object DomainSocketAddressSerializer : KSerializer<DomainSocketAddress> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("DomainSocketAddressSerializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): DomainSocketAddress {
    val items = decoder.decodeString().split(':')
    return DomainSocketAddress(
      host = items[0],
      port = items[1].toInt(),
    )
  }

  override fun serialize(encoder: Encoder, value: DomainSocketAddress) {
    encoder.encodeString("${value.host}:${value.port}")
  }
}
