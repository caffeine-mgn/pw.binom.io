package pw.binom.db

import pw.binom.io.AsyncCloseable

interface AsyncConnection : AsyncCloseable {
    fun createStatement(): AsyncStatement
    fun prepareStatement(query: String): AsyncPreparedStatement
    suspend fun commit()
    suspend fun rollback()
    val type: String
}