package pw.binom.db.sync

import pw.binom.db.async.DatabaseInfo
import pw.binom.io.Closeable

interface SyncConnection:Closeable{
    fun createStatement(): SyncStatement
    fun prepareStatement(query: String): SyncPreparedStatement
    fun beginTransaction()
    fun commit()
    fun rollback()
    val type: String
    val isConnected:Boolean
    val dbInfo: DatabaseInfo
}