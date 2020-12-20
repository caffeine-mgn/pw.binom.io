package pw.binom.db

import pw.binom.io.Closeable

interface SyncStatement : Closeable {
    val connection: SyncConnection
    fun executeQuery(query: String): SyncResultSet
    fun executeUpdate(query: String)
}