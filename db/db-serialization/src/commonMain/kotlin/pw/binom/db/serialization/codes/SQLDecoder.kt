package pw.binom.db.serialization.codes

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.serialization.SQLCompositeDecoder
import pw.binom.uuid.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SQLDecoder : Decoder {
  fun decodeDateTime(): DateTime

  fun decodeDate(): Date

  fun decodeUUID(): UUID
  @OptIn(ExperimentalUuidApi::class)
  fun decodeUuid(): Uuid

  fun decodeByteArray(): ByteArray

  override fun beginStructure(descriptor: SerialDescriptor): SQLCompositeDecoder
}
