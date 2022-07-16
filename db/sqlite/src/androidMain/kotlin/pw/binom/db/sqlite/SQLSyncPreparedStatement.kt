package pw.binom.db.sqlite

import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase.CursorFactory
import pw.binom.date.DateTime
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncResultSet

class SQLSyncPreparedStatement(val query: String, override val connection: SQLiteConnector) : SyncPreparedStatement {

    private val params = arrayOfNulls<Any?>(query.count { it == '?' })

    private val cursorFactory = CursorFactory { db, masterQuery, editTable, query ->
        params.forEachIndexed { index, value ->
            when (value) {
                null -> query.bindNull(index + 1)
                is String -> query.bindString(index + 1, value)
                is Int -> query.bindLong(index + 1, value.toLong())
                is Long -> query.bindLong(index + 1, value)
                is ByteArray -> query.bindBlob(index + 1, value)
            }
        }
        SQLiteCursor(masterQuery, editTable, query)
    }

    override fun set(index: Int, value: Double) {
        params[index] = value.toString()
    }

    override fun set(index: Int, value: Float) {
        set(
            index = index,
            value = value.toDouble(),
        )
    }

    override fun set(index: Int, value: Int) {
        params[index] = value
    }

    override fun set(index: Int, value: Long) {
        params[index] = value
    }

    override fun set(index: Int, value: String) {
        params[index] = value
    }

    override fun set(index: Int, value: Boolean) {
        set(
            index = index,
            value = if (value) 1 else 0,
        )
    }

    override fun set(index: Int, value: ByteArray) {
        params[index] = value
    }

    override fun set(index: Int, value: DateTime) {
        set(index, value.time)
    }

    override fun setNull(index: Int) {
        params[index] = null
    }

    override fun executeQuery(): SyncResultSet {
        val cursor = connection.native.rawQueryWithFactory(
            cursorFactory,
            query,
            null,
            null,
        )
        return AndroidSyncResultSet(cursor)
    }

    override fun executeUpdate(): Long {
        connection.native.rawQueryWithFactory(
            cursorFactory,
            query,
            null,
            null,
        ).close()
        return connection.getChanges()
    }

    override fun close() {
    }
}
