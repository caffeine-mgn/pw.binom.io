package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime
import pw.binom.db.serialization.codes.SQLDecoder
import pw.binom.db.serialization.codes.SQLEncoder

object DateSerializer : KSerializer<DateTime> {
    override fun deserialize(decoder: Decoder): DateTime {
        if (decoder !is SQLDecoder) {
            throw IllegalArgumentException("DateSerializer support only pw.binom.db.serialization.SqlDecoder")
        }
        return decoder.decodeDateTime()
    }

    override fun serialize(encoder: Encoder, value: DateTime) {
        if (encoder !is SQLEncoder) {
            throw IllegalArgumentException("DateSerializer support only pw.binom.db.serialization.SQLEncoder")
        }
        encoder.encodeDate(value)
    }

    override val descriptor: SerialDescriptor
        get() = Long.serializer().descriptor
}
