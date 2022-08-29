package pw.binom.db.serialization

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmInline

interface DBAccess2 {
    suspend fun <T : Any> insert(k: KSerializer<T>, value: T, excludeGenerated: Boolean = false)
    suspend fun <T : Any> insertAndReturn(k: KSerializer<T>, value: T, excludeGenerated: Boolean = false): T
    suspend fun <T : Any> select(k: KSerializer<T>, func: suspend QueryContext.() -> String): Flow<T>
    suspend fun update(func: suspend QueryContext.() -> String): Long
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess2.insert(value: T, excludeGenerated: Boolean = false) =
    insert(k = T::class.serializer(), value = value, excludeGenerated = excludeGenerated)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess2.insertAndReturn(value: T, excludeGenerated: Boolean = false) =
    insertAndReturn(k = T::class.serializer(), value = value, excludeGenerated = excludeGenerated)

interface QueryContext {
    val serializersModule: SerializersModule
    fun <T : Any> param(k: KSerializer<T>, value: T?): String
}

interface UpdateContext : QueryContext {
    fun <T : Any> returning(k: KSerializer<T>, func: suspend (T) -> Unit)
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> UpdateContext.returning(noinline func: suspend (T) -> Unit) =
    returning(k = T::class.serializer(), func = func)

@JvmInline
value class EntityColumns(val columns: String) {
    override fun toString(): String = columns
}

/*
fun QueryContext.columns(k: KSerializer<out Any>, prefix: String = ""): EntityColumns {

}

private fun generateColumnNames(
    prefix: String,
    forceUseQuotes: Boolean,
    withoutQuotes: Boolean,
    list: MutableList<String>,
    filter: (EntityDescription.Column) -> Boolean
) {
    columns.forEach { (name, column) ->
        if (!filter(column)) {
            return@forEach
        }
//            if (column.serialDescriptor.kind is PrimitiveKind) {
//            }
        val embedded = serialDescriptor.getElementAnnotation<Embedded>(column.index) != null
        if (embedded) {
            val splitter = serialDescriptor.getElementAnnotation<EmbeddedSplitter>(column.index)?.splitter ?: "_"
            descriptorContext.getDescription(column.serialDescriptor).generateColumnNames(
                prefix = prefix + name + splitter,
                forceUseQuotes = column.useQuotes || forceUseQuotes,
                list = list,
                filter = filter,
                withoutQuotes = withoutQuotes,
            )
        } else {
            list += if ((column.useQuotes || forceUseQuotes) && !withoutQuotes) {
                "\"$prefix$name\""
            } else {
                "$prefix$name"
            }
        }
    }
}
*/

internal fun getTableName(descriptor: SerialDescriptor): String {
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

fun QueryContext.tableName(descriptor: SerialDescriptor): String = getTableName(descriptor)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> QueryContext.tableName(): String = tableName(T::class.serializer().descriptor)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> QueryContext.param(value: T?) = param(
    k = this.serializersModule.getContextual(T::class)
        ?: T::class.serializerOrNull()
        ?: throw SerializationException("Can't find serializer for ${T::class}"),
    value = value
)
