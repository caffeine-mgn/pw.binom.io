package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XmlElement

@OptIn(ExperimentalSerializationApi::class)
class XmlDecoder(val root: XmlElement, override val serializersModule: SerializersModule) : AbstractDecoder() {
  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    return XmlObjectDecoder(root = root, serializersModule = serializersModule, descriptor = descriptor)
  }

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    TODO("Not yet implemented")
  }

  override fun decodeString(): String = root.body ?: throw SerializationException("Tag ${root.tag} not have any body")
  override fun decodeLong(): Long {
    val str = decodeString()
    return str.toLongOrNull() ?: throw SerializationException("Can't convert \"$str\" to Long")
  }

  //    override fun decodeString(): String = root.body ?: throw SerializationException("Tag ${root.tag} not have any body")
}
