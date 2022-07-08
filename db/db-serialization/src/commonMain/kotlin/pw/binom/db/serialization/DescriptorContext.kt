package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor

interface DescriptorContext {
    fun getDescription(serialDescriptor: SerialDescriptor): EntityDescription
    fun getDescription(serializer: KSerializer<out Any>): EntityDescription =
        getDescription(serializer.descriptor)
}
