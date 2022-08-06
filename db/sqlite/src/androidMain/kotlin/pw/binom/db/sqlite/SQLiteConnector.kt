package pw.binom.db.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncStatement
import pw.binom.io.file.File
import java.io.File as JFile

actual class SQLiteConnector(val native: SQLiteDatabase) : SyncConnection {
    actual companion object {
        /**
         * @param mode Open mode. Use values like [Context.MODE_PRIVATE]
         */
        fun openInternal(context: Context, name: String, mode: Int): SQLiteConnector =
            SQLiteConnector(context.openOrCreateDatabase(name, mode, null))

        actual fun openFile(file: File): SQLiteConnector = SQLiteConnector(
            SQLiteDatabase.openOrCreateDatabase(
                JFile(file.toString()),
                null
            )
        )

        actual fun memory(name: String?): SQLiteConnector =
            SQLiteConnector(SQLiteDatabase.createInMemory(SQLiteDatabase.OpenParams.Builder().build()))

        actual val TYPE: String
            get() = "SQLite"
    }

    override val type: String
        get() = TYPE
    override val isConnected: Boolean
        get() = native.isOpen
    override val dbInfo: DatabaseInfo
        get() = SQLiteSQLDatabaseInfo

    internal fun getChanges(): Long {
        native.rawQuery("select changes()", null).use {
            if (it.moveToNext()) {
                return it.getLong(0)
            }
        }
        return 0
    }

    override fun createStatement(): SyncStatement = SQLiteSyncStatement(this)

    override fun prepareStatement(query: String): SyncPreparedStatement = SQLSyncPreparedStatement(
        query = query,
        connection = this,
    )

    override fun beginTransaction() {
        native.beginTransactionNonExclusive()
    }

    override fun commit() {
        native.setTransactionSuccessful()
        native.endTransaction()
    }

    override fun rollback() {
        println("before rollback--------------------------inTransaction=${native.inTransaction()}!")
        native.endTransaction()
        println("after rollback--------------------------inTransaction=${native.inTransaction()}!")
    }

    override fun close() {
        native.close()
    }
}
