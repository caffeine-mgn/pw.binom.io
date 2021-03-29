package pw.binom.db.pool

import pw.binom.db.AsyncConnection
import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.AsyncResultSet

interface PooledAsyncConnection: AsyncConnection {
    fun usePreparedStatement(sql: String): AsyncPreparedStatement
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