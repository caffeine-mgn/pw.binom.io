package pw.binom.db.sqlite

import pw.binom.db.sync.SyncConnection
import pw.binom.io.file.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SQLiteConnector : SyncConnection {
  companion object {
    fun openFile(file: File): SQLiteConnector
    fun memory(name: String? = null): SQLiteConnector
    val TYPE: String
  }
}
