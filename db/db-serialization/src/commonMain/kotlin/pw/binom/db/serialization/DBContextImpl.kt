package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.pool.AsyncConnectionPool

internal class DBContextImpl(val pool: AsyncConnectionPool, val sql: SQLSerialization) : DBContext {
    private val tx = TransactionManagerImpl(pool)
    val statements = HashMap<String, SQLQueryNamedArguments>()
    val mappers = HashMap<KSerializer<out Any>, suspend (AsyncResultSet) -> Any>()
    override suspend fun <T> re(function: suspend (DBAccess) -> T): T = tx.re {
        val access = DBAccessImpl(this, it, sql)
        function(access)
    }

    override suspend fun <T> su(function: suspend (DBAccess) -> T): T = tx.su {
        val access = DBAccessImpl(this, it, sql)
        function(access)
    }

    override suspend fun <T> new(function: suspend (DBAccess) -> T): T = tx.new {
        val access = DBAccessImpl(this, it, sql)
        function(access)
    }

    override suspend fun asyncClose() {
        pool.asyncClose()
    }
}