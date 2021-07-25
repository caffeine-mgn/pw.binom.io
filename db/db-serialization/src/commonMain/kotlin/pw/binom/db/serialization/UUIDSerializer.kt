package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.UUID

object UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID {
        if (decoder !is SQLValueDecoder) {
            throw IllegalArgumentException("UUIDSerializer support only pw.binom.db.serialization.SQLValueDecoder")
        }
        return decoder.resultSet.getUUID(decoder.columnName)!!
    }

    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: UUID) {
        if (encoder !is SQLValueEncoder) {
            throw IllegalArgumentException("UUIDSerializer support only pw.binom.db.serialization.SQLValueEncoder")
        }

        encoder.classDescriptor.getElementName(encoder.fieldIndex)
        encoder.map[encoder.columnName] = value
    }
}