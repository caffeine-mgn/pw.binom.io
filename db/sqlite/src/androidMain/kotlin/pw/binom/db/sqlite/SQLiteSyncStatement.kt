package pw.binom.db.sqlite

import pw.binom.db.SQLException
import pw.binom.db.sync.SyncResultSet
import pw.binom.db.sync.SyncStatement

class SQLiteSyncStatement(override val connection: SQLiteConnector) : SyncStatement {

    override fun executeQuery(query: String): SyncResultSet =
        AndroidSyncResultSet(connection.native.rawQuery(query, null))

    override fun executeUpdate(query: String): Long {
        var changes = 0L
        SqlUtils.splitQueryStatements(query).forEach { singleQuery ->
            try {
                connection.native.execSQL(singleQuery)
            } catch (e: Throwable) {
                throw SQLException("Fail to execute \"$singleQuery\"", e)
            }
            changes += connection.getChanges()
        }
        return changes
        val ff = connection.native.rawQuery(query, null)!!
        ff.use { ff ->
            ff.moveToFirst()
            var sb = ""
            repeat(ff.columnCount) { index ->
                sb += "  " + (ff.getColumnName(index) ?: "null")
            }
            while (ff.moveToNext()) {
                var sb = ""
                repeat(ff.columnCount) { index ->
                    sb += "  " + (ff.getString(index) ?: "null")
                }
            }
        }
        return connection.getChanges()
    }

    override fun close() {
    }
}
