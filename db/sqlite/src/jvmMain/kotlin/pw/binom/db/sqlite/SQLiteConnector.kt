package pw.binom.db.sqlite

import org.sqlite.jdbc4.JDBC4Connection
import pw.binom.db.SQLException
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.file.File
import java.util.*

actual class SQLiteConnector(internal val native: JDBC4Connection) : SyncConnection {
  actual companion object {
    actual fun openFile(file: File): SQLiteConnector {
      val connection = JDBC4Connection(null, file.path, Properties())
      return SQLiteConnector(connection)
    }

    actual fun memory(name: String?): SQLiteConnector {
      val connection = JDBC4Connection("jdbc:sqlite::memory:${name ?: ""}", "", Properties())
      return SQLiteConnector(connection)
    }

    actual val TYPE: String
      get() = "SQLite"
  }

  actual override val type: String
    get() = TYPE
  actual override val isConnected: Boolean
    get() = !native.isClosed
  actual override val dbInfo: DatabaseInfo
    get() = SQLiteSQLDatabaseInfo

  private val beginPt = native.prepareStatement("begin")
  private val commitPt = native.prepareStatement("commit")
  private val rollbackPt = native.prepareStatement("rollback")

  init {
    native.autoCommit = false
    commitPt.executeUpdate()
  }

  actual override fun createStatement(): SyncStatement =
    SQLiteSyncStatement(this)

  actual override fun prepareStatement(query: String): SyncPreparedStatement =
    try {
      SQLSyncPreparedStatement(this, native.prepareStatement(query))
    } catch (e: Throwable) {
      throw SQLException("Can't execute query \"$query\"", e)
    }

  actual override fun beginTransaction() {
    beginPt.executeUpdate()
  }

  actual override fun commit() {
    commitPt.executeUpdate()
//        native.transactionIsolation
//        native.commit()
  }

  actual override fun rollback() {
    rollbackPt.executeUpdate()
//        native.rollback()
  }

  actual override fun close() {
    native.close()
  }
}
