package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.jvm.JvmName

@get:JvmName("getIdColumn2")
@OptIn(ExperimentalSerializationApi::class)
val KSerializer<out Any>.idColumn: String?
    get() {
        for (i in 0 until descriptor.elementsCount) {
            descriptor.getElementAnnotations(i).forEach {
                if (it is Id) {
                    return descriptor.getElementName(i)
                }
            }
        }
        return null
    }

fun KSerializer<out Any>.getIdColumn() =
    idColumn ?: throw IllegalArgumentException("Can't find Id Column in ${descriptor.serialName}")

inline fun <reified T : Any> SerialDescriptor.getElementAnnotation(index: Int) =
    getElementAnnotations(index).find { it is T }?.let { it as T }

fun SerialDescriptor.isUseQuotes(index: Int) =
    getElementAnnotation<UseQuotes>(index) != null

fun SerialDescriptor.isUseQuotes() =
    annotations.any { it is UseQuotes }
