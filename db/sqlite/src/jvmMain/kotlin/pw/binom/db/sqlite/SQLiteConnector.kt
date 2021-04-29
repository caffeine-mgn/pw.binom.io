package pw.binom.db.sqlite

import org.sqlite.jdbc4.JDBC4Connection
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.file.File
import pw.binom.io.file.FileNotFoundException
import pw.binom.io.file.isExist
import pw.binom.io.file.parent
import java.util.*

actual class SQLiteConnector(internal val native: JDBC4Connection) : SyncConnection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector {
            val parent = file.parent ?: throw FileNotFoundException("File ${file.path} not found")
            if (!parent.isExist) {
                throw FileNotFoundException("Direction ${file.path} is not found")
            }
            if (!parent.isDirectory) {
                throw FileNotFoundException("Path ${file.path} is not direction")
            }
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
    override val isConnected: Boolean
        get() = !native.isClosed

    override fun close() {
        native.close()
    }
}