package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
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
import pw.binom.xml.dom.XmlElement

class XmlDecoder2(
  override val serializersModule: SerializersModule,
  val element: XElement.Tag,
  val config: XmlConfig,
) : AbstractDecoder() {

  companion object {
    private fun find(tag: XElement.Tag, context: NameSpaceContext, descriptor: SerialDescriptor): SerialDescriptor? {
      return when (descriptor.kind) {
        PolymorphicKind.SEALED -> find(tag = tag, context = context, descriptor = descriptor.getElementDescriptor(1))
        SerialKind.CONTEXTUAL -> {
          descriptor.elementDescriptors.asSequence()
            .firstNotNullOf { find(tag = tag, context = context, descriptor = it) }
        }

        else -> TODO()
      }
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
    val ctx = NameSpaceContext.fromTag(element)
    return when (descriptor.kind) {
      PolymorphicKind.SEALED -> SealXmlDecoder2(
        descriptor = descriptor,
        serializersModule = serializersModule,
        context = ctx,
        element = element,
        config = config,
      )

      StructureKind.CLASS -> XmlClassDecoder(
        descriptor = descriptor,
        serializersModule = serializersModule,
        context = ctx,
        element = element,
        config = config,
      )

      else -> TODO()
    }

    find(element, ctx, descriptor)

    if (descriptor.kind == PolymorphicKind.SEALED) {
      val subClasses = descriptor.getElementDescriptor(1) // getting sealed classes
      check(subClasses.kind == SerialKind.CONTEXTUAL)
      return beginStructure(subClasses)//subClasses.elementDescriptors
    }

    if (descriptor.kind == SerialKind.CONTEXTUAL) {
      TODO()
    }

    val name = descriptor.xmlName()
    val nameWithoutPrefix = element.nameWithoutPrefix
    if (nameWithoutPrefix != name) {
      throw SerializationException("Unknown tag name: excepted \"$name\", but found \"$nameWithoutPrefix\"")
    }
    return XmlObjectDecoder2(
      root = element,
      serializersModule = serializersModule,
      context = ctx,
      descriptor = descriptor,
    )
  }

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    TODO("Not yet implemented")
  }
}
