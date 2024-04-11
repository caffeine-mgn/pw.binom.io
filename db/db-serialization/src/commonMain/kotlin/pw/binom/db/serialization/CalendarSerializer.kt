package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.Calendar

object CalendarSerializer : KSerializer<Calendar> {
  override fun deserialize(decoder: Decoder): Calendar {
    if (decoder !is SQLValueDecoder) {
      throw IllegalArgumentException("CalendarSerializer support only pw.binom.db.serialization.SQLValueDecoder")
    }
    return decoder.resultSet.getDateTime(decoder.columnName)!!.calendar()
  }

  override fun serialize(
    encoder: Encoder,
    value: Calendar,
  ) {
    if (encoder !is SQLValueEncoder) {
      throw IllegalArgumentException("CalendarSerializer support only pw.binom.db.serialization.SQLValueEncoder")
    }

    encoder.classDescriptor.getElementName(encoder.fieldIndex)
    encoder.map[encoder.columnName] = value.dateTime
  }

  override val descriptor: SerialDescriptor
    get() = Long.serializer().descriptor
}
