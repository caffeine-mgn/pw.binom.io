package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.jvm.JvmName

@OptIn(ExperimentalSerializationApi::class)
val <T : Any> KSerializer<T>.tableName: String
    get() {
        val descriptor = descriptor
        val useQuotes = descriptor.annotations.any { it is UseQuotes }
        descriptor.annotations.forEach {
            if (it is TableName) {
                return if (useQuotes) {
                    "\"${it.tableName}\""
                } else {
                    it.tableName
                }

            }
        }
        return if (useQuotes) {
            "\"${descriptor.serialName}\""
        } else {
            descriptor.serialName
        }

    }

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