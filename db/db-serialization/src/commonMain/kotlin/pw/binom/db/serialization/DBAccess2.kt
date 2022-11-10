package pw.binom.db.serialization

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmInline

interface DBAccess2 {
    suspend fun <T : Any> insert(k: KSerializer<T>, value: T, excludeGenerated: Boolean = true)
    suspend fun <T : Any> insertAndReturn(k: KSerializer<T>, value: T, excludeGenerated: Boolean = true): T
    suspend fun <T : Any> select(k: KSerializer<T>, func: suspend QueryContext.() -> String): Flow<T>
    suspend fun update(func: suspend QueryContext.() -> String): Long

    suspend fun <T : Any> selectAll(k: KSerializer<T>, condition: (suspend QueryContext.() -> String)? = null): Flow<T>
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
    val useQuotes = descriptor.isUseQuotes()
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

internal fun internalGenerateSelectColumns(
    tableName: String,
    descriptor: SerialDescriptor,
    prefix: String,
): String {
    val sb = StringBuilder()
    internalGenerateSelectColumns(
        tableName = tableName,
        descriptor = descriptor,
        prefix = prefix,
        prefix2 = "$tableName.",
        useQuotes = false,
        output = sb
    )
    return sb.toString()
}

fun QueryContext.columns(
    descriptor: SerialDescriptor,
    tableName: String = tableName(descriptor),
    prefix: String = descriptor.getTableName() ?: descriptor.serialName
) = internalGenerateSelectColumns(
    tableName = tableName,
    descriptor = descriptor,
    prefix = prefix,
)

fun QueryContext.columns(
    serializer: KSerializer<out Any>,
    tableName: String = tableName(serializer.descriptor),
    prefix: String = serializer.descriptor.getTableName() ?: serializer.descriptor.serialName
) = columns(
    descriptor = serializer.descriptor,
    tableName = tableName,
    prefix = prefix,
)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> QueryContext.columns(
    tableName: String = tableName(T::class.serializer().descriptor),
    prefix: String = T::class.serializer().descriptor.getTableName() ?: T::class.serializer().descriptor.serialName
) = columns(
    serializer = T::class.serializer(),
    tableName = tableName,
    prefix = prefix,
)

private fun internalGenerateSelectColumns(
    tableName: String,
    descriptor: SerialDescriptor,
    prefix: String,
    prefix2: String,
    useQuotes: Boolean,
    output: StringBuilder
) {
    val tableNameUseQuotes = useQuotes || descriptor.isUseQuotes()
    repeat(descriptor.elementsCount) { index ->
        val elementName = descriptor.getElementName(index)
        val isEmbedded = descriptor.getElementAnnotation<Embedded>(index) != null
        val isUseQuotes = useQuotes || descriptor.isUseQuotes(index)
        if (isEmbedded) {
            val embeddedSpliter = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: "_"
            internalGenerateSelectColumns(
                tableName = tableName,
                descriptor = descriptor.getElementDescriptor(index),
                prefix = "$prefix$elementName$embeddedSpliter",
                useQuotes = useQuotes || isUseQuotes,
                output = output,
                prefix2 = "$prefix2$elementName$embeddedSpliter",
            )
            return@repeat
        }
        if (output.isNotEmpty()) {
            output.append(",")
        }

        output.append(prefix2)
        if (isUseQuotes) {
            output.append("\"")
        }
        output.append(elementName)
        if (isUseQuotes) {
            output.append("\"")
        }
        output.append(" as ")

        if (isUseQuotes) {
            output.append("\"")
        }
        output.append(prefix).append(elementName)
        if (isUseQuotes) {
            output.append("\"")
        }
    }
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> QueryContext.param(value: T?) = param(
    k = this.serializersModule.getContextual(T::class)
        ?: T::class.serializerOrNull()
        ?: throw SerializationException("Can't find serializer for ${T::class}"),
    value = value
)
