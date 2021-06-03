package pw.binom.db.async

import pw.binom.io.AsyncCloseable
import pw.binom.io.use

interface AsyncConnection : AsyncCloseable {
    val type: String
    val isConnected: Boolean

    suspend fun createStatement(): AsyncStatement
    suspend fun prepareStatement(query: String): AsyncPreparedStatement
    fun isReadyForQuery(): Boolean
    suspend fun commit()
    suspend fun rollback()
    suspend fun executeUpdate(sql: String) =
        createStatement().use {
            it.executeUpdate(sql)
        }
}