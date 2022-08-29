package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import pw.binom.db.serialization.codes.SQLDecoder

interface SQLCompositeDecoder : CompositeDecoder {
    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLDecoder
}
