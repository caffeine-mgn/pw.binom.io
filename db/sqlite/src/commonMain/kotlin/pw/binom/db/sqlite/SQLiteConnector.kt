package pw.binom.db.sqlite

import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.file.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SQLiteConnector : SyncConnection {
  companion object {
    fun openFile(file: File): SQLiteConnector
    fun memory(name: String? = null): SQLiteConnector
    val TYPE: String
  }

  override fun close()
  override val dbInfo: DatabaseInfo
  override val isConnected: Boolean
  override val type: String
  override fun beginTransaction()
  override fun commit()
  override fun prepareStatement(query: String): SyncPreparedStatement
  override fun createStatement(): SyncStatement
  override fun rollback()
}
