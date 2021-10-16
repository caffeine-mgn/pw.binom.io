package pw.binom.db.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.db.DatabaseEngine
import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.io.AsyncCloseable

interface DBContext : AsyncCloseable {
    /**
     * Use current transaction if exist. If current transaction not exist will create new transaction
     */
    suspend fun <T> re(function: suspend (DBAccess) -> T): T

    /**
     * Use current transaction if exist. If current transaction not exist will work outside transaction
     */
    suspend fun <T> su(function: suspend (DBAccess) -> T): T

    /**
     * Always creates new transaction
     */
    suspend fun <T> new(function: suspend (DBAccess) -> T): T

    /**
     * Creates schema for [serializer]. For generation used method [generateSchema]
     */
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
     *
     * @param ifNotExist add special flag "if not exist" for creating sql
     */
    fun generateSchema(
        engine: DatabaseEngine,
        serializer: KSerializer<out Any>,
        ifNotExist: Boolean = true,
        tableName: String? = null
    ): String

    companion object {
        fun create(pool: AsyncConnectionPool, sql: SQLSerialization = SQLSerialization.DEFAULT): DBContext =
            DBContextImpl(pool = pool, sql = sql)
    }
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> DBContext.createSchema(ifNotExist: Boolean = true, tableName: String? = null) {
    createSchema(T::class.serializer(), ifNotExist = ifNotExist, tableName = tableName)
}