package pw.binom.xml.serialization

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

class XmlListEncoder(
    val store: XmlElement,
    val tagName: String,
    val ns: String?,
    val classDiscriminator: String,
    override val serializersModule: SerializersModule
) : AbstractEncoder() {

    private var descriptor: SerialDescriptor? = null
    private var index: Int = 0

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        this.descriptor = descriptor
        this.index = index
        return true
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        val descriptor = this.descriptor
        if (descriptor != null) {
            if (descriptor.kind is StructureKind.LIST) {
                val el = XmlElement(
                    tag = tagName,
                    nameSpace = ns,
                )
                val encoder = XmlObjectEncoder(
                    body = el,
                    classDiscriminator = classDiscriminator,
                    serializersModule = serializersModule
                )
                el.parent = store
                serializer.serialize(encoder, value)
            } else {
                TODO("Not yet implemented")
            }
        } else {
            TODO()
        }
    }
}
