package pw.binom.db.sqlite

import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.file.File

actual class SQLiteConnector : SyncConnection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector {
            TODO()
//            SQLiteDatabase.openOrCreateDatabase(
//                JFile(file.toString()),
//                SQLiteDatabase.CursorFactory { db, masterQuery, editTable, query ->
//                    SQLiteCursor
//                }
//            )
//            SQLiteOpenHelper
        }

        actual fun memory(name: String?): SQLiteConnector {
            TODO()
        }

        actual val TYPE: String
            get() = ""
    }

    override fun createStatement(): SyncStatement {
        TODO("Not yet implemented")
    }

    override fun prepareStatement(query: String): SyncPreparedStatement {
        TODO("Not yet implemented")
    }

    override fun beginTransaction() {
        TODO("Not yet implemented")
    }

    override fun commit() {
        TODO("Not yet implemented")
    }

    override fun rollback() {
        TODO("Not yet implemented")
    }

    override val type: String
        get() = TODO("Not yet implemented")
    override val isConnected: Boolean
        get() = TODO("Not yet implemented")
    override val dbInfo: DatabaseInfo
        get() = TODO("Not yet implemented")

    override fun close() {
        TODO("Not yet implemented")
    }
}
