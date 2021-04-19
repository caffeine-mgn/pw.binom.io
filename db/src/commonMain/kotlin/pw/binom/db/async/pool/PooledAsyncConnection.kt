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