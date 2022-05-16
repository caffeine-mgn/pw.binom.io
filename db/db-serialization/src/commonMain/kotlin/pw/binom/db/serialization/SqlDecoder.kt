package pw.binom.db.serialization

import kotlinx.serialization.encoding.Decoder
import pw.binom.UUID
import pw.binom.date.Date

interface SqlDecoder : Decoder {
    fun decodeDate(): Date
    fun decodeUUID(): UUID
    fun decodeByteArray(): ByteArray
}
