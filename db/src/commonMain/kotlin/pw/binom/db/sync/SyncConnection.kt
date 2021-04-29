package pw.binom.db.sync

import pw.binom.io.Closeable

interface SyncConnection:Closeable{
    fun createStatement(): SyncStatement
    fun prepareStatement(query: String): SyncPreparedStatement
    fun commit()
    fun rollback()
    val type: String
    val isConnected:Boolean
}