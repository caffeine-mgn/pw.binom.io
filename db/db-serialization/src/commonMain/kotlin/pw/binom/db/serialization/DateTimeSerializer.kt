package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime
import pw.binom.db.serialization.codes.SQLDecoder
import pw.binom.db.serialization.codes.SQLEncoder
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DateTime::class)
object DateTimeSerializer : KSerializer<DateTime> {

  override val descriptor: SerialDescriptor = Long.serializer().descriptor

  override fun deserialize(decoder: Decoder): DateTime {
    if (decoder !is SQLDecoder) {
      return DateTime(decoder.decodeLong())
    }
    return decoder.decodeDateTime()
  }

  override fun serialize(
    encoder: Encoder,
    value: DateTime,
  ) {
    if (encoder !is SQLEncoder) {
      encoder.encodeLong(value.milliseconds)
      return
    }
    encoder.encodeDate(value)
  }
}
