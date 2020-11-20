package pw.binom.db

import pw.binom.io.Closeable

interface Connection : Closeable {
    fun createStatement(): SyncStatement
    fun prepareStatement(query: String): SyncPreparedStatement
    fun commit()
    fun rollback()
    val type: String
}