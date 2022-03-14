package pw.binom.db.serialization

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        if (decoder !is SQLValueDecoder) {
            throw IllegalArgumentException("BigDecimalSerializer support only pw.binom.db.serialization.SQLValueDecoder")
        }
        return decoder.resultSet.getBigDecimal(decoder.columnName)!!
    }

    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        if (encoder !is SQLValueEncoder) {
            throw IllegalArgumentException("BigDecimalSerializer support only pw.binom.db.serialization.SQLValueEncoder")
        }

        encoder.classDescriptor.getElementName(encoder.fieldIndex)
        encoder.map[encoder.columnName] = value
    }
}
