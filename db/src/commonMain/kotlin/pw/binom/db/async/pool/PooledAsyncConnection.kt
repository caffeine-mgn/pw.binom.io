package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement

interface PooledAsyncConnection : AsyncConnection {
    suspend fun usePreparedStatement(sql: String): AsyncPreparedStatement
    suspend fun closePreparedStatement(sql: String)
}
