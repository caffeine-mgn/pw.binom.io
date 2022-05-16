package pw.binom.db.serialization

import kotlinx.serialization.encoding.Encoder
import pw.binom.UUID
import pw.binom.date.Date

interface SQLEncoder : Encoder {
    fun encodeDate(date: Date)
    fun encodeUUID(uuid: UUID)
    fun encodeByteArray(array: ByteArray)
}
