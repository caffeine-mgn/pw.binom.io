package pw.binom.db

import pw.binom.io.AsyncCloseable
import pw.binom.io.use

interface AsyncConnection : AsyncCloseable {
    val type: String
    val isConnected: Boolean

    fun createStatement(): AsyncStatement
    fun prepareStatement(query: String): AsyncPreparedStatement
    suspend fun commit()
    suspend fun rollback()
    suspend fun executeUpdate(sql: String) =
        createStatement().use {
            it.executeUpdate(sql)
        }
}