package pw.binom.db.serialization.codes

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import pw.binom.UUID
import pw.binom.date.DateTime
import pw.binom.db.serialization.SQLCompositeEncoder

interface SQLEncoder : Encoder {
    fun encodeDate(dateTime: DateTime)
    fun encodeUUID(uuid: UUID)
    fun encodeByteArray(array: ByteArray)
    override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeEncoder

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): SQLEncoder
}
