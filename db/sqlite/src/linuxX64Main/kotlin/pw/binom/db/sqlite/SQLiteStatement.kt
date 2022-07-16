package pw.binom.db.sqlite

import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import platform.internal_sqlite.SQLITE_OK
import platform.internal_sqlite.sqlite3_changes
import platform.internal_sqlite.sqlite3_errmsg
import platform.internal_sqlite.sqlite3_exec
import pw.binom.db.SQLException
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncResultSet
import pw.binom.db.sync.SyncStatement

private class AResultSet(val source: SyncResultSet, val statement: SyncPreparedStatement) : SyncResultSet by source {
    override fun close() {
        source.close()
        statement.close()
    }
}

class SQLiteStatement(override val connection: SQLiteConnector) : SyncStatement {

    override fun executeQuery(query: String): SyncResultSet {
        return connection.prepareStatement(query).let { c ->
            AResultSet(c.executeQuery(), c)
        }
    }

    override fun executeUpdate(query: String): Long {
        if (sqlite3_exec(connection.ctx.pointed.value, query, null, null, null) != SQLITE_OK) {
            val errno = sqlite3_errmsg(connection.ctx.pointed.value)?.toKStringFromUtf8()
            if (errno == null) {
                throw SQLException()
            } else {
                throw SQLException(errno)
            }
        }
        return sqlite3_changes(connection.ctx.pointed.value).toLong()
    }

    override fun close() {
    }
}
