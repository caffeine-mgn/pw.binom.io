package pw.binom.db.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface DBAccess {
    suspend fun <T : Any> select(query: String, vararg args: Pair<String, Any?>, result: KSerializer<T>): List<T>
    suspend fun <T : Any> selectFrom(
        from: KSerializer<T>,
        queryCondition: String? = null,
        vararg args: Pair<String, Any?>
    ): List<T>

    suspend fun deleteFrom(
        from: KSerializer<out Any>,
        queryCondition: String? = null,
        vararg args: Pair<String, Any?>
    ): Long

    suspend fun <T : Any> upsert(
        serializer: KSerializer<T>,
        value: T,
        excludeUpdate: Set<String> = emptySet(),
        updateOnly: Set<String> = emptySet()
    )

    suspend fun update(query: String, vararg args: Pair<String, Any?>): Long
    suspend fun <T : Any> find(serializer: KSerializer<T>, key: Any): T?
    suspend fun delete(serializer: KSerializer<out Any>, id: Any): Boolean

    /**
     * @param byColumns columns for select conditions. By default (when no define any columns) use field with `@Id`
     */
    suspend fun <T : Any> update(serializer: KSerializer<T>, value: T, vararg byColumns: String): Boolean
    suspend fun <T : Any> insert(serializer: KSerializer<T>, value: T): Boolean
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.delete(value: T) =
    delete(T::class.serializer(), value)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.update(value: T, vararg byColumns: String) =
    update(serializer = T::class.serializer(), value = value, byColumns = byColumns)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.insert(value: T) =
    insert(T::class.serializer(), value)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.deleteFrom(
    queryCondition: String? = null,
    vararg args: Pair<String, Any?>
) =
    deleteFrom(from = T::class.serializer(), queryCondition = queryCondition, args = args)

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.upsert(
    value: T,
    excludeUpdate: Set<String> = emptySet(),
    updateOnly: Set<String> = emptySet()
) =
    upsert(
        serializer = T::class.serializer(),
        value = value,
        excludeUpdate = excludeUpdate,
        updateOnly = updateOnly
    )

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBAccess.selectFrom(
    queryCondition: String? = null,
    vararg args: Pair<String, Any?>
) =
    selectFrom(from = T::class.serializer(), queryCondition = queryCondition, args = args)