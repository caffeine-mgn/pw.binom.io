package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import pw.binom.db.serialization.codes.SQLEncoder

interface SQLCompositeEncoder : CompositeEncoder {
    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLEncoder
}
