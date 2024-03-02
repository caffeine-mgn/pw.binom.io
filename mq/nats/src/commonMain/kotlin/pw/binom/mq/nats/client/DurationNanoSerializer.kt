package pw.binom.mq.nats.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

object DurationNanoSerializer : KSerializer<Duration> {
  override val descriptor: SerialDescriptor
    get() = Long.serializer().descriptor

  override fun deserialize(decoder: Decoder): Duration = decoder.decodeLong().nanoseconds

  override fun serialize(
    encoder: Encoder,
    value: Duration,
  ) {
    encoder.encodeLong(value.inWholeNanoseconds)
  }
}
