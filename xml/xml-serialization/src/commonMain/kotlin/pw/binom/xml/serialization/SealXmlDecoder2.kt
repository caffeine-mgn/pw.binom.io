package pw.binom.xml.serialization

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import pw.binom.xml.dom.XElement

class SealXmlDecoder2(
  val element: XElement.Tag,
  val descriptor: SerialDescriptor,
  val context: NameSpaceContext,
  val config: XmlConfig,
  override val serializersModule: SerializersModule,
) : AbstractDecoder() {

  companion object {
    private const val NONE = -1
    private const val READ_TYPE = 0
    private const val READ_VALUE = 1
  }

  private val elements: List<SerialDescriptor>

  init {
    require(descriptor.kind == PolymorphicKind.SEALED)
    val contextual = descriptor.getElementDescriptor(1)
    require(contextual.kind == SerialKind.CONTEXTUAL)
    elements = contextual.elementDescriptors.toList()
  }

  private var state = NONE
  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    val newState = when (state) {
      NONE -> READ_TYPE
      READ_TYPE -> READ_VALUE
      READ_VALUE -> CompositeDecoder.DECODE_DONE
      else -> return CompositeDecoder.UNKNOWN_NAME
    }
    state = newState
    return newState
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    return when (descriptor.kind) {
      StructureKind.CLASS -> XmlClassDecoder(
        element = element,
        descriptor = descriptor,
        context = context,
        serializersModule = serializersModule,
        config = config,
      )

      else -> TODO()
    }
    return super.beginStructure(descriptor)
  }

  override fun decodeString(): String {
    val name = element.nameWithoutPrefix
    val nameSpace = context.getNameSpace(element)
    return when (state) {
      READ_TYPE -> {
        elements.forEach { el ->
          if (el.xmlName() == name && el.xmlNamespace()?.firstOrNull() == nameSpace) {
            return el.serialName
          }
        }
        throw SerializationException("Can't find \"${name}\" in scope of ${descriptor.serialName}")
      }

      else -> TODO()
    }
  }
}
