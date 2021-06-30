package pw.binom.db.async.pool

import pw.binom.db.SQLQueryNamedArguments
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.map
import pw.binom.io.AsyncCloseable
import pw.binom.io.use
import kotlin.jvm.JvmInline

class PooledAsyncPreparedStatement2(val pool: AsyncConnectionPoolImpl, val sql: String) : AsyncCloseable {
    override suspend fun asyncClose() {
        pool.free(sql)
    }

    suspend fun executeQuery(connection: PooledAsyncConnectionImpl, vararg args: Any?) =
        connection.usePreparedStatement(sql).executeQuery(*args)

    suspend fun executeUpdate(connection: PooledAsyncConnectionImpl, vararg args: Any?) =
        connection.usePreparedStatement(sql).executeUpdate(*args)
}

@JvmInline
value class SelectQuery(val query: SQLQueryNamedArguments) {

    constructor(sql: String) : this(SQLQueryNamedArguments.parse(sql))

    suspend fun <T> execute(
        connection: PooledAsyncConnection,
        vararg args: Pair<String, Any?>,
        processing: suspend (AsyncResultSet) -> T
    ) =
        connection.usePreparedStatement(query.sql).executeQuery(*query.buildArguments(*args)).use {
            processing(it)
        }

    fun <T : Any> mapper(mapper: suspend (AsyncResultSet) -> T) =
        SelectQueryWithMapper(query, mapper)
}

@JvmInline
value class UpdateQuery(val query: SQLQueryNamedArguments) {
    constructor(sql: String) : this(SQLQueryNamedArguments.parse(sql))
    suspend fun execute(connection: PooledAsyncConnection, vararg args: Pair<String, Any?>): Long =
        connection.usePreparedStatement(query.sql).executeUpdate(*query.buildArguments(*args))

    fun <T : Any> args(applier: suspend (T) -> Array<Pair<String, Any?>>) =
        UpdateQueryWithParam(
            query = query,
            applier = applier
        )
}

suspend fun <T : Any> PooledAsyncConnection.selectOneOrNull(
    query: SelectQueryWithMapper<T>,
    vararg args: Pair<String, Any?>
): T? =
    usePreparedStatement(query.query.sql).executeQuery(*query.query.buildArguments(*args)).use {
        if (it.next()) {
            query.mapper(it)
        } else {
            null
        }
    }

suspend fun <T : Any> PooledAsyncConnection.update(query: UpdateQueryWithParam<T>, value: T) =
    usePreparedStatement(query.query.sql).executeUpdate(*query.query.buildArguments(*query.applier(value)))

suspend fun <T : Any> PooledAsyncConnection.selectAll(
    query: SelectQueryWithMapper<T>,
    vararg args: Pair<String, Any?>
): List<T> =
    usePreparedStatement(query.query.sql).executeQuery(*query.query.buildArguments(*args)).use {
        it.map(query.mapper)
    }
class SelectQueryWithMapper<T : Any>(val query: SQLQueryNamedArguments, val mapper: suspend (AsyncResultSet) -> T)
class UpdateQueryWithParam<T : Any>(
    val query: SQLQueryNamedArguments,
    val applier: suspend (T) -> Array<Pair<String, Any?>>
)

suspend fun <T> PooledAsyncConnection.execute(
    query: SelectQuery,
    vararg args: Pair<String, Any?>,
    processing: suspend (AsyncResultSet) -> T
): T =
    query.execute(this, *args) { processing(it) }

suspend fun PooledAsyncConnection.execute(
    query: UpdateQuery,
    vararg args: Pair<String, Any?>,
) =
    query.execute(this, *args)