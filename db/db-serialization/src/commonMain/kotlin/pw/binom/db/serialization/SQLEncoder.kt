package pw.binom.db.serialization

import kotlinx.serialization.encoding.Encoder
import pw.binom.UUID
import pw.binom.date.DateTime

interface SQLEncoder : Encoder {
    fun encodeDate(dateTime: DateTime)
    fun encodeUUID(uuid: UUID)
    fun encodeByteArray(array: ByteArray)
}
