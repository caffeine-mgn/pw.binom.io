@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.db.serialization

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import pw.binom.db.async.AsyncResultSet
import kotlin.jvm.JvmInline

interface DBAccess2 {
  val ctx: DBContext

  sealed interface ActionOnConflict {
    @Deprecated(message = "For internal use", level = DeprecationLevel.HIDDEN)
    interface OnColumns {
      val columns: Array<String>
    }

    object DoNothing : ActionOnConflict

    @Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR")
    class DoNothingOnColumns(vararg columns: String) : ActionOnConflict, OnColumns {
      override val columns: Array<String> = columns as Array<String>
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR")
    class DoUpdateOnColumns(vararg columns: String) : ActionOnConflict, OnColumns {
      override val columns: Array<String> = columns as Array<String>
    }

    object DoUpdate : ActionOnConflict

    object DoThrow : ActionOnConflict
  }

  val serializersModule: SerializersModule

  suspend fun <T : Any> insert(
    serializer: KSerializer<T>,
    value: T,
    excludeGenerated: Boolean = true,
    onConflict: ActionOnConflict = ActionOnConflict.DoThrow,
  ): Boolean

  suspend fun <T : Any> insertAndReturn(
    serializer: KSerializer<T>,
    value: T,
    excludeGenerated: Boolean = true,
    onConflict: ActionOnConflict = ActionOnConflict.DoThrow,
  ): T?

  suspend fun <T : Any> select(
    serializer: KSerializer<T>,
    func: suspend SelectContext.() -> String,
  ): Flow<T>

  suspend fun selectRaw(func: suspend SelectContext.() -> String): AsyncResultSet

  suspend fun update(func: suspend QueryContext.() -> String): Long

  suspend fun <T : Any> selectAll(
    serializer: KSerializer<T>,
    condition: (suspend QueryContext.() -> String)? = null,
  ): Flow<T>
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess2.insert(
  value: T,
  excludeGenerated: Boolean = true,
) = insert(serializer = T::class.serializer(), value = value, excludeGenerated = excludeGenerated)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess2.insertAndReturn(
  value: T,
  excludeGenerated: Boolean = true,
) = insertAndReturn(serializer = T::class.serializer(), value = value, excludeGenerated = excludeGenerated)

interface WhereContext {
  fun and(func: QueryContext.() -> String): String
  fun or(func: QueryContext.() -> String): String
}

interface QueryContext {
  val serializersModule: SerializersModule

  fun <T : Any> param(
    serializer: KSerializer<T>,
    value: T?,
  ): String

  operator fun String.unaryPlus(): String
}

interface RootQueryContext : QueryContext {
  fun where(func: WhereContext.() -> String): String
}

interface SelectContext : RootQueryContext

interface UpdateContext : RootQueryContext {
  fun <T : Any> returning(
    serializer: KSerializer<T>,
    func: suspend (T) -> Unit,
  )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> UpdateContext.returning(noinline func: suspend (T) -> Unit) =
  returning(serializer = T::class.serializer(), func = func)

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
  onlyIndexed: Boolean,
): String {
  val sb = StringBuilder()
  internalGenerateSelectColumns(
    descriptor = descriptor,
    prefix = prefix,
    prefix2 = "$tableName.",
    useQuotes = false,
    output = sb,
    onlyIndexed = onlyIndexed,
  )
  return sb.toString()
}

fun QueryContext.columns(
  descriptor: SerialDescriptor,
  tableName: String = tableName(descriptor),
  prefix: String = descriptor.getTableName() ?: descriptor.serialName,
) = internalGenerateSelectColumns(
  tableName = tableName,
  descriptor = descriptor,
  prefix = prefix,
  onlyIndexed = false,
)

fun QueryContext.columns(
  serializer: KSerializer<out Any>,
  tableName: String = tableName(serializer.descriptor),
  prefix: String = serializer.descriptor.getTableName() ?: serializer.descriptor.serialName,
) = columns(
  descriptor = serializer.descriptor,
  tableName = tableName,
  prefix = prefix,
)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> QueryContext.columns(
  tableName: String = tableName(T::class.serializer().descriptor),
  prefix: String = T::class.serializer().descriptor.getTableName() ?: T::class.serializer().descriptor.serialName,
) = columns(
  serializer = T::class.serializer(),
  tableName = tableName,
  prefix = prefix,
)

interface EntityVisitor {
  fun entity(descriptor: SerialDescriptor): EntityVisitor = this

  fun end() {}

  fun column(
    descriptor: SerialDescriptor,
    elementIndex: Int,
    index: Boolean,
    name: String,
    autoGenerated: Boolean,
    id: Boolean,
    useQuotes: Boolean,
  ) {
  }

  fun embedded(
    descriptor: SerialDescriptor,
    elementIndex: Int,
    index: Boolean,
    name: String,
    useQuotes: Boolean,
    id: Boolean,
    splitter: String,
    autoGenerated: Boolean,
  ): EntityVisitor = this
}

fun SerialDescriptor.accept(visitor: EntityVisitor) {
  val descriptor = this
  val v = visitor.entity(this)
  val useQuotes = descriptor.isUseQuotes()
  repeat(descriptor.elementsCount) { index ->
    val isId = descriptor.getElementAnnotation<Id>(index) != null
    val isIndex = descriptor.getElementAnnotation<IndexColumn>(index) != null
    val autoGenerated = descriptor.getElementAnnotation<AutoGenerated>(index) != null
    val elementDescriptor = descriptor.getElementDescriptor(index)
    val elementName = descriptor.getElementName(index)
    val isEmbedded = descriptor.getElementAnnotation<Embedded>(index) != null
    val isUseQuotes = useQuotes || descriptor.isUseQuotes(index)
    if (isEmbedded) {
      val embeddedVisitor =
        v.embedded(
          descriptor = elementDescriptor,
          elementIndex = index,
          index = isIndex,
          name = elementName,
          useQuotes = isUseQuotes,
          splitter = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: "_",
          autoGenerated = autoGenerated,
          id = isId,
        )
      val columnVisitor = embeddedVisitor.entity(elementDescriptor)
      elementDescriptor.accept(columnVisitor)
      return@repeat
    } else {
      v.column(
        descriptor = elementDescriptor,
        elementIndex = index,
        index = isIndex,
        name = elementName,
        useQuotes = isUseQuotes,
        autoGenerated = autoGenerated,
        id = isId,
      )
    }
    v.end()
  }
}

internal fun internalGenerateSelectColumns(
  descriptor: SerialDescriptor,
  prefix: String,
  prefix2: String,
  useQuotes: Boolean,
  output: StringBuilder,
  onlyIndexed: Boolean,
) {
  repeat(descriptor.elementsCount) { index ->
    if (onlyIndexed && descriptor.getElementAnnotation<IndexColumn>(index) == null) {
      return@repeat
    }
    val elementName = descriptor.getElementName(index)
    val isEmbedded = descriptor.getElementAnnotation<Embedded>(index) != null
    val isUseQuotes = useQuotes || descriptor.isUseQuotes(index)
    var first = true
    if (isEmbedded) {
      val embeddedSpliter = descriptor.getElementAnnotation<EmbeddedSplitter>(index)?.splitter ?: "_"
      internalGenerateSelectColumns(
        descriptor = descriptor.getElementDescriptor(index),
        prefix = "$prefix$elementName$embeddedSpliter",
        useQuotes = useQuotes || isUseQuotes,
        output = output,
        prefix2 = "$prefix2$elementName$embeddedSpliter",
        onlyIndexed = onlyIndexed,
      )
      return@repeat
    }
    if (!first) {
      output.append(",")
    }

    first = false
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
inline fun <reified T : Any> QueryContext.param(value: T?) =
  param(
    serializer =
    this.serializersModule.getContextual(T::class) ?: T::class.serializerOrNull()
    ?: throw SerializationException("Can't find serializer for ${T::class}"),
    value = value,
  )
