package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.Date

object DateSerializer : KSerializer<Date> {
    override fun deserialize(decoder: Decoder): Date {
        if (decoder !is SqlDecoder) {
            throw IllegalArgumentException("DateSerializer support only pw.binom.db.serialization.SqlDecoder")
        }
        return decoder.decodeDate()
    }

    override fun serialize(encoder: Encoder, value: Date) {
        if (encoder !is SQLEncoder) {
            throw IllegalArgumentException("DateSerializer support only pw.binom.db.serialization.SQLEncoder")
        }
        encoder.encodeDate(value)
    }

    override val descriptor: SerialDescriptor
        get() = Long.serializer().descriptor
}
