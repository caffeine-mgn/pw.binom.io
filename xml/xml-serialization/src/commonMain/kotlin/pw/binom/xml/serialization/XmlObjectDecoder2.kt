package pw.binom.xml.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XElement

class XmlObjectDecoder2(
  override val serializersModule: SerializersModule,
  val context: NameSpaceContext,
  val root: XElement.Tag,
  val descriptor: SerialDescriptor
) : AbstractDecoder() {
  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    TODO("Not yet implemented")
  }

}
