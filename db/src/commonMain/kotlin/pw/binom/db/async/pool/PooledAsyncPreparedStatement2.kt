package pw.binom.db.async.pool

import pw.binom.db.async.AsyncResultSet
import pw.binom.io.AsyncCloseable
import pw.binom.io.use

class PooledAsyncPreparedStatement2(val pool: AsyncConnectionPool, val sql: String) : AsyncCloseable {
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
}

inline class UpdateQuery(val sql: String) {
    suspend fun execute(connection: PooledAsyncConnection, vararg args: Any?): Long =
        connection.usePreparedStatement(sql).executeUpdate(*args)
}