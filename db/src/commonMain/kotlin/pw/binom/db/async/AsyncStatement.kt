package pw.binom.db.async

import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncResultSet
import pw.binom.io.AsyncCloseable

interface AsyncStatement:AsyncCloseable {
    val connection: AsyncConnection
    suspend fun executeQuery(query: String): AsyncResultSet
    suspend fun executeUpdate(query: String): Long
}