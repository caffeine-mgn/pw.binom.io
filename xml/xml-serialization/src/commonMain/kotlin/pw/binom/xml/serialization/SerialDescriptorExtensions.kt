@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.xml.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.xml.serialization.annotations.XmlName
import pw.binom.xml.serialization.annotations.XmlNamespace

fun SerialDescriptor.xmlName() =
    (annotations.find { it is XmlName } as XmlName?)?.name ?: serialName

fun SerialDescriptor.xmlName(index: Int) =
    (getElementAnnotations(index).find { it is XmlName } as XmlName?)?.name
        ?: getElementName(index)

fun SerialDescriptor.xmlNamespace() =
    (annotations.find { it is XmlNamespace } as XmlNamespace?)?.ns?.takeIf { it.isNotEmpty() }

fun SerialDescriptor.xmlNamespace(index: Int) =
    (getElementAnnotations(index).find { it is XmlNamespace } as XmlNamespace?)?.ns?.takeIf { it.isNotEmpty() }

inline fun <reified T : Any> SerialDescriptor.getElementAnnotation(index: Int) =
    (getElementAnnotations(index).find { it is T } as T?)
