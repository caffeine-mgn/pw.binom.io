package pw.binom.db.sqlite

import org.sqlite.jdbc4.JDBC4Connection
import pw.binom.db.Connection
import pw.binom.db.SyncPreparedStatement
import pw.binom.db.SyncStatement
import pw.binom.io.file.File
import java.util.*

actual class SQLiteConnector(internal val native: JDBC4Connection) : Connection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector {
            val connection = JDBC4Connection(null, file.path, Properties())
            return SQLiteConnector(connection)
        }

        actual fun memory(name: String?): SQLiteConnector {
            val connection = JDBC4Connection("jdbc:sqlite::memory:", null, Properties())
            return SQLiteConnector(connection)
        }

        actual val TYPE: String
            get() = "SQLite"
    }

    init {
        native.autoCommit = false
    }

    override fun createStatement(): SyncStatement =
            SQLiteSyncStatement(this)

    override fun prepareStatement(query: String): SyncPreparedStatement =
            SQLSyncPreparedStatement(this, native.prepareStatement(query))

    override fun commit() {
        native.commit()
    }

    override fun rollback() {
        native.rollback()
    }

    override val type: String
        get() = TYPE

    override fun close() {
        native.close()
    }
}