package pw.binom.db.serialization.codes

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.serialization.*

class SQLCompositeDecoderImpl(val ctx: SQLDecoderPool, val onClose: () -> Unit) : SQLCompositeDecoder {
    override var serializersModule: SerializersModule = EmptySerializersModule
    var input: DataProvider = DataProvider.EMPTY
    var prefix = ""
    var cursor = 0

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        input.getBoolean(prefix + descriptor.getElementName(index))

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        input.getByte(prefix + descriptor.getElementName(index))

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        input.getChar(prefix + descriptor.getElementName(index))

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        input.getDouble(prefix + descriptor.getElementName(index))

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (cursor == descriptor.elementsCount) {
            return CompositeDecoder.DECODE_DONE
        }
        for (it in cursor until descriptor.elementsCount) {
            if (descriptor.getElementDescriptor(it).kind is StructureKind) {
                return cursor++
            } else {
                val column = prefix + descriptor.getElementName(it)

                if (input.contains(column)) {
                    return cursor++
                }
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        input.getFloat(prefix + descriptor.getElementName(index))

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        input.getInt(prefix + descriptor.getElementName(index))

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        input.getLong(prefix + descriptor.getElementName(index))

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val embedded = descriptor.getElementAnnotation<Embedded>(index)
        val embedded2 = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: "_"
        if (embedded != null) {
            val fieldDescriptor = descriptor.getElementDescriptor(index)
            val exist = (0 until fieldDescriptor.elementsCount).any { fieldIndex ->
                val elementName = fieldDescriptor.getElementName(fieldIndex)
                val el = "$prefix${descriptor.getElementName(index)}$embedded2$elementName"
                input.contains(el) && !input.isNull(el)
            }
            if (!exist) {
                return previousValue
            }
            val c = ctx.decoderValue(
                name = "$prefix${descriptor.getElementName(index)}",
                input = input,
                serializersModule = serializersModule
            )
            return deserializer.deserialize(c)
//            val decoder = SQLValueDecoder(
//                classDescriptor = descriptor,
//                fieldIndex = index,
//                columnPrefix = columnPrefix,
//                resultSet = resultSet,
//                serializersModule = serializersModule
//            )
//            return deserializer.deserialize(decoder)
        }
        return if (input.isNull(prefix + descriptor.getElementName(index))) {
            previousValue
        } else {
            val c = ctx.decoderValue(
                name = prefix + descriptor.getElementName(index),
                input = input,
                serializersModule = serializersModule
            )
            return deserializer.deserialize(c)
//            val decoder = SQLValueDecoder(
//                classDescriptor = descriptor,
//                fieldIndex = index,
//                columnPrefix = columnPrefix,
//                resultSet = resultSet,
//                serializersModule = serializersModule
//            )
//            deserializer.deserialize(decoder)
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val embedded2 = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: ""
        val decoder = ctx.decoderValue(
            name = prefix + descriptor.getElementName(index) + embedded2,
            input = input,
            serializersModule = serializersModule
        )
        return deserializer.deserialize(decoder)
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        input.getShort(prefix + descriptor.getElementName(index))

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        input.getString(prefix + descriptor.getElementName(index))

    override fun endStructure(descriptor: SerialDescriptor) {
        onClose()
        // Do nothing
    }

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): SQLDecoder {
        TODO("Not yet implemented")
    }
}
