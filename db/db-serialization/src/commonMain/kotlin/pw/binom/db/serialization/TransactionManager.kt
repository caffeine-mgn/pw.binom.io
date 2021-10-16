package pw.binom.db.serialization

import pw.binom.db.async.pool.PooledAsyncConnection

interface TransactionManager {
    suspend fun <T> re(function: suspend (PooledAsyncConnection) -> T): T
    suspend fun <T> new(function: suspend (PooledAsyncConnection) -> T): T
    suspend fun <T> su(function: suspend (PooledAsyncConnection) -> T): T
}