@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.xml.serialization.annotations.XmlEmptyValue
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace

fun SerialDescriptor.xmlName() =
    (annotations.find { it is XmlName } as XmlName?)?.name ?: serialName

fun SerialDescriptor.xmlName(index: Int) =
//    if (this is PrimitiveKind) {
//        null
//    } else {
    (getElementAnnotations(index).find { it is XmlName } as XmlName?)?.name
        ?: getElementName(index)
//    }

fun SerialDescriptor.xmlEmptyValue(index: Int) =
    (getElementAnnotations(index).find { it is XmlEmptyValue } as XmlEmptyValue?)?.value

fun SerialDescriptor.xmlNamespace() =
    (annotations.find { it is XmlNamespace } as XmlNamespace?)?.ns?.takeIf { it.isNotEmpty() }

fun SerialDescriptor.xmlNamespace(index: Int) =
    (getElementAnnotations(index).find { it is XmlNamespace } as XmlNamespace?)?.ns?.takeIf { it.isNotEmpty() }

inline fun <reified T : Any> SerialDescriptor.getElementAnnotation(index: Int) =
    (getElementAnnotations(index).find { it is T } as T?)
