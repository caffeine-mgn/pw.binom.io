package pw.binom.db.sync

import pw.binom.io.Closeable

interface SyncStatement : Closeable {
    val connection: SyncConnection
    fun executeQuery(query: String): SyncResultSet
    fun executeUpdate(query: String): Long
}