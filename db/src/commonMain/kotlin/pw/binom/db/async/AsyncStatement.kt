package pw.binom.db.async

import pw.binom.io.AsyncCloseable

interface AsyncStatement : AsyncCloseable {
    val connection: AsyncConnection
    suspend fun executeQuery(query: String): AsyncResultSet
    suspend fun executeUpdate(query: String): Long
}
