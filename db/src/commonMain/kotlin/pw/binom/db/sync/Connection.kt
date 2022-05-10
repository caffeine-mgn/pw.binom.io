package pw.binom.db.sync

import pw.binom.io.Closeable

interface Connection : Closeable {
    val type: String
    val isConnected: Boolean
    fun createStatement(): SyncStatement
    fun prepareStatement(query: String): SyncPreparedStatement
    fun commit()
    fun rollback()
}
