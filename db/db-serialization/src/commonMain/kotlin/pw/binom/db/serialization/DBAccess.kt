package pw.binom.db.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.db.async.DatabaseInfo

interface DBAccess {
    suspend fun <T : Any> select(query: String, vararg args: Pair<String, Any?>, result: KSerializer<T>): List<T>

    suspend fun update(query: String, vararg args: Pair<String, Any?>): Long

    val dbDatabaseInfo: DatabaseInfo

    /**
     * Replace [value] or insert if [value] already exist
     * Make sure you defined annotation [IndexColumn] for index columns
     */
    suspend fun <T : Any> upsertEntity(
        serializer: KSerializer<T>,
        value: T,
        excludeUpdate: Set<String> = emptySet(),
        updateOnly: Set<String> = emptySet()
    )

    suspend fun deleteEntityFrom(
        from: KSerializer<out Any>,
        queryCondition: String? = null,
        args: Array<out Pair<String, Any?>> = emptyArray(),
        tableName: String? = null,
    ): Long

    suspend fun <T : Any> selectEntityFrom(
        fromSerializer: KSerializer<T>,
        queryCondition: String? = null,
        args: Array<out Pair<String, Any?>> = emptyArray(),
        tableName: String? = null,
    ): List<T>

    suspend fun <T : Any> find(
        serializer: KSerializer<T>,
        key: Any,
        tableName: String? = null,
    ): T?

    suspend fun deleteEntityFrom(
        serializer: KSerializer<out Any>,
        id: Any,
        tableName: String? = null,
    ): Boolean

    /**
     * @param byColumns columns for select conditions. By default (when no define any columns) use field with `@Id`
     */
    suspend fun <T : Any> updateEntity(
        serializer: KSerializer<T>,
        value: T,
        tableName: String? = null,
        includeColumns: Array<String> = emptyArray(),
        excludeColumns: Array<String> = emptyArray(),
        byColumns: Array<String> = emptyArray()
    ): Boolean

    suspend fun <T : Any> insertEntity(
        tableName: String? = null,
        serializer: KSerializer<T>,
        value: T,
    ): Boolean
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.updateEntity(
    value: T,
    tableName: String? = null,
    byColumns: Array<String> = emptyArray(),
    excludeColumns: Array<String> = emptyArray(),
    includeColumns: Array<String> = emptyArray(),
) =
    updateEntity(
        serializer = T::class.serializer(),
        value = value,
        tableName = tableName,
        excludeColumns = excludeColumns,
        includeColumns = includeColumns,
        byColumns = byColumns
    )

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.insertEntity(value: T, tableName: String? = null) =
    insertEntity(serializer = T::class.serializer(), value = value, tableName = tableName)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.deleteEntityFrom(
    queryCondition: String? = null,
    tableName: String? = null,
    args: Array<out Pair<String, Any?>> = emptyArray(),
) =
    deleteEntityFrom(from = T::class.serializer(), queryCondition = queryCondition, args = args, tableName = tableName)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.upsertEntity(
    value: T,
    excludeUpdate: Set<String> = emptySet(),
    updateOnly: Set<String> = emptySet()
) =
    upsertEntity(
        serializer = T::class.serializer(),
        value = value,
        excludeUpdate = excludeUpdate,
        updateOnly = updateOnly
    )

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.selectEntityFrom(
    queryCondition: String? = null,
    args: Array<Pair<String, Any?>> = emptyArray(),
    tableName: String? = null,
) =
    selectEntityFrom(
        fromSerializer = T::class.serializer(),
        queryCondition = queryCondition,
        args = args,
        tableName = tableName
    )