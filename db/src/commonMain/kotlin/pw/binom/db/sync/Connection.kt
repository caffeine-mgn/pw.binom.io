package pw.binom.db.sync

import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.Closeable

interface Connection : Closeable {
    val type: String
    val isConnected: Boolean
    fun createStatement(): SyncStatement
    fun prepareStatement(query: String): SyncPreparedStatement
    fun commit()
    fun rollback()
}