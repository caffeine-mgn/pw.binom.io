package pw.binom.uuid.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import pw.binom.uuid.UUID

object UUIDLongSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "UUID",
    ) {
        element<Long>("most")
        element<Long>("least")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): UUID = decoder.decodeStructure(descriptor) {
        var most = 0L
        var least = 0L
        if (decodeSequentially()) {
            most = decodeLongElement(descriptor, 0)
            least = decodeLongElement(descriptor, 1)
        } else {
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> most = decodeLongElement(descriptor, 0)
                    1 -> least = decodeLongElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
        }
        UUID(most, least)
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.mostSigBits)
            encodeLongElement(descriptor, 1, value.leastSigBits)
        }
    }
}
