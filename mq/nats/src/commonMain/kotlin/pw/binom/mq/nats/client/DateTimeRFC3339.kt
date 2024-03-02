package pw.binom.mq.nats.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime
import pw.binom.date.format.toDatePattern
import pw.binom.date.parseIso8601Date

object DateTimeRFC3339 : KSerializer<DateTime> {
  private val pattern =
    "yyyy-MM-dd'T'HH:mm:ss.(SSSSSSSSS|SSSSSSSS|SSSSSSS|SSSSSS|@SSS|SS|S)(XXX|XX|X|z)".toDatePattern()
  override val descriptor: SerialDescriptor = String.serializer().descriptor

  fun decode(value: String) =
    pattern.parseOrNull(value) ?: value.parseIso8601Date()
      ?: throw SerializationException("Can't parse \"$value\" to date as RFC 3339")

  override fun deserialize(decoder: Decoder): DateTime {
    return decode(decoder.decodeString())
  }

  override fun serialize(
    encoder: Encoder,
    value: DateTime,
  ) {
    encoder.encodeString(pattern.toString(value))
  }
}
