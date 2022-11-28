package pw.binom.db.serialization.codes

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import pw.binom.date.DateTime
import pw.binom.db.serialization.SQLCompositeDecoder
import pw.binom.uuid.UUID

interface SQLDecoder : Decoder {
    fun decodeDateTime(): DateTime
    fun decodeUUID(): UUID
    fun decodeByteArray(): ByteArray
    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeDecoder
}
