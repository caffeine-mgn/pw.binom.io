package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet

interface PooledAsyncConnection: AsyncConnection {
    suspend fun usePreparedStatement(sql: String): AsyncPreparedStatement
    suspend fun closePreparedStatement(sql: String)

    suspend fun <T> execute(
        query: SelectQuery,
        vararg args: Any?,
        processing: suspend (AsyncResultSet) -> T
    ): T =
        query.execute(this, *args) { processing(it) }

    suspend fun execute(
        query: UpdateQuery,
        vararg args: Any?,
    ) =
        query.execute(this, *args)
}