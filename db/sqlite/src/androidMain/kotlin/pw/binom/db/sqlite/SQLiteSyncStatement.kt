package pw.binom.db.sqlite

import android.util.Log
import pw.binom.db.sync.SyncResultSet
import pw.binom.db.sync.SyncStatement

fun log(text: String) {
    Log.d("pw.binom.db", text)
}

class SQLiteSyncStatement(override val connection: SQLiteConnector) : SyncStatement {

    override fun executeQuery(query: String): SyncResultSet =
        AndroidSyncResultSet(connection.native.rawQuery(query, null))

    override fun executeUpdate(query: String): Long {
        log("Executing \"$query\"")
        connection.native.execSQL(query)
        return connection.getChanges()
        log("1------------------")
        val ff = connection.native.rawQuery(query, null)!!
        ff.use { ff ->
            ff.moveToFirst()
            log("result count: ${ff.count}")
            var sb = ""
            repeat(ff.columnCount) { index ->
                sb += "  " + (ff.getColumnName(index) ?: "null")
            }
            while (ff.moveToNext()) {
                var sb = ""
                repeat(ff.columnCount) { index ->
                    sb += "  " + (ff.getString(index) ?: "null")
                }
                log(sb)
            }
        }
        log("2------------------")
        return connection.getChanges()
    }

    override fun close() {
    }
}
