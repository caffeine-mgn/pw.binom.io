package pw.binom.db.serialization

import kotlinx.serialization.encoding.Decoder
import pw.binom.UUID
import pw.binom.date.DateTime

interface SqlDecoder : Decoder {
    fun decodeDate(): DateTime
    fun decodeUUID(): UUID
    fun decodeByteArray(): ByteArray
}
