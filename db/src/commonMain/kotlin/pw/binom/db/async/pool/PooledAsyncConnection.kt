package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.map
import pw.binom.io.use

interface PooledAsyncConnection : AsyncConnection {
    suspend fun usePreparedStatement(sql: String): AsyncPreparedStatement
    suspend fun closePreparedStatement(sql: String)
}

suspend fun <T> PooledAsyncConnection.execute(
    query: SelectQuery,
    vararg args: Any?,
    processing: suspend (AsyncResultSet) -> T
): T =
    query.execute(this, *args) { processing(it) }

suspend fun <T : Any> PooledAsyncConnection.selectOneOrNull(query: SelectQueryWithMapper<T>, vararg args: Any?): T? =
    usePreparedStatement(query.sql).executeQuery(*args).use {
        if (it.next()) {
            query.mapper(it)
        } else {
            null
        }
    }

suspend fun <T : Any> PooledAsyncConnection.update(query: UpdateQueryWithParam<T>, value: T) =
    usePreparedStatement(query.sql).executeUpdate(*query.applier(value))

suspend fun <T : Any> PooledAsyncConnection.selectAll(query: SelectQueryWithMapper<T>, vararg args: Any?): List<T> =
    usePreparedStatement(query.sql).executeQuery(*args).use {
        it.map(query.mapper)
    }

suspend fun PooledAsyncConnection.execute(
    query: UpdateQuery,
    vararg args: Any?,
) =
    query.execute(this, *args)