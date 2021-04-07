package pw.binom.db.async.pool

import pw.binom.db.async.AsyncResultSet
import pw.binom.io.AsyncCloseable
import pw.binom.io.use

class PooledAsyncPreparedStatement2(val pool: AsyncConnectionPoolImpl, val sql: String) : AsyncCloseable {
    override suspend fun asyncClose() {
        pool.free(sql)
    }

    suspend fun executeQuery(connection: PooledAsyncConnectionImpl, vararg args: Any?) =
        connection.usePreparedStatement(sql).executeQuery(*args)

    suspend fun executeUpdate(connection: PooledAsyncConnectionImpl, vararg args: Any?) =
        connection.usePreparedStatement(sql).executeUpdate(*args)
}

inline class SelectQuery(val sql: String) {
    suspend fun <T> execute(
        connection: PooledAsyncConnection,
        vararg args: Any?,
        processing: suspend (AsyncResultSet) -> T
    ) =
        connection.usePreparedStatement(sql).executeQuery(*args).use {
            processing(it)
        }

    fun <T : Any> mapper(mapper: suspend (AsyncResultSet) -> T) =
        SelectQueryWithMapper(sql, mapper)
}

inline class UpdateQuery(val sql: String) {
    suspend fun execute(connection: PooledAsyncConnection, vararg args: Any?): Long =
        connection.usePreparedStatement(sql).executeUpdate(*args)

    fun <T : Any> args(applier: suspend (T) -> Array<Any?>) =
        UpdateQueryWithParam(
            sql = sql,
            applier = applier
        )
}

class SelectQueryWithMapper<T : Any>(val sql: String, val mapper: suspend (AsyncResultSet) -> T)
class UpdateQueryWithParam<T : Any>(val sql: String, val applier: suspend (T) -> Array<Any?>)