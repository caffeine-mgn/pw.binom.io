package pw.binom.s3.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime
import pw.binom.date.iso8601
import pw.binom.date.parseIso8601Date

object DateSerialization : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): DateTime = decoder.decodeString().parseIso8601Date(0)!!

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.iso8601(0))
    }
}
