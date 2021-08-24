package pw.binom.db.serialization

import kotlinx.serialization.KSerializer
import pw.binom.db.DatabaseEngine
import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.io.AsyncCloseable

interface DBContext:AsyncCloseable {
    suspend fun <T> re(function: suspend (DBAccess) -> T): T
    suspend fun <T> su(function: suspend (DBAccess) -> T): T
    suspend fun <T> new(function: suspend (DBAccess) -> T): T
    suspend fun createSchema(serializer: KSerializer<out Any>, ifNotExist: Boolean = true, tableName: String? = null)

    /**
     * Generates table schema.
     * Make sure you defined annotation:
     * * [TableName]
     * * [ColumnType]
     * * [PGColumnType]
     * * [SqliteColumnType],
     * * [IndexColumn]
     * * [Index]
     */
    fun generateSchema(engine: DatabaseEngine,serializer: KSerializer<out Any>, ifNotExist: Boolean = true, tableName: String? = null): String

    companion object {
        fun create(pool: AsyncConnectionPool, sql: SQLSerialization = SQLSerialization.DEFAULT): DBContext =
            DBContextImpl(pool = pool, sql = sql)
    }
}