package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.Date

object DateSerializer : KSerializer<Date> {
    override fun deserialize(decoder: Decoder): Date {
        if (decoder !is SQLValueDecoder) {
            throw IllegalArgumentException("UUIDSerializer support only pw.binom.db.serialization.SQLValueDecoder")
        }
        return decoder.resultSet.getDate(decoder.columnName)!!
    }

    override fun serialize(encoder: Encoder, value: Date) {
        if (encoder !is SQLValueEncoder) {
            throw IllegalArgumentException("UUIDSerializer support only pw.binom.db.serialization.SQLValueEncoder")
        }

        encoder.classDescriptor.getElementName(encoder.fieldIndex)
        encoder.map[encoder.columnName] = value
    }

    override val descriptor: SerialDescriptor
        get() = Long.serializer().descriptor
}
