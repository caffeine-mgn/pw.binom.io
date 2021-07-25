package pw.binom.db.serialization

import pw.binom.db.async.pool.AsyncConnectionPool

interface DBContext {
    suspend fun <T> re(function: suspend (DBAccess) -> T): T
    suspend fun <T> su(function: suspend (DBAccess) -> T): T
    suspend fun <T> new(function: suspend (DBAccess) -> T): T

    companion object {
        fun create(pool: AsyncConnectionPool, sql: SQLSerialization = SQLSerialization.DEFAULT): DBContext =
            DBContextImpl(pool = pool, sql = sql)
    }
}