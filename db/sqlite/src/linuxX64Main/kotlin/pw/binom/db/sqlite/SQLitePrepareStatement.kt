package pw.binom.db.sqlite

import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import platform.posix.free
import pw.binom.db.SyncPreparedStatement
import pw.binom.db.*

class SQLitePrepareStatement(override val connection: SQLiteConnector,
                             internal val native: CPointer<CPointerVar<sqlite3_stmt>>
) : SyncPreparedStatement {

    internal var openedResultSetCount = 0

    private inline val stmt
        get() = native[0]

    private inline val maxParams
        get() = sqlite3_limit(connection.ctx.pointed.value, SQLITE_LIMIT_VARIABLE_NUMBER, -1)

    private inline fun checkRange(index: Int) {
        if (index < 0 || index >= maxParams)
            throw ArrayIndexOutOfBoundsException()
    }

    override fun set(index: Int, value: Float) {
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_double(stmt, index + 1, value.toDouble()))
    }

    override fun set(index: Int, value: Int) {
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_int(stmt, index + 1, value))
    }

    override fun set(index: Int, value: Long) {
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_int64(stmt, index + 1, value))
    }

    override fun set(index: Int, value: String) {
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value, -1, null))
    }

    override fun set(index: Int, value: Boolean) {
        set(index, if (value) 1 else 0)
    }

    override fun set(index: Int, value: ByteArray) {
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_blob(stmt, index + 1, value.refTo(0), value.size, SQLITE_STATIC))
    }

    override fun executeQuery(): SyncResultSet {
        val code = sqlite3_step(stmt)
        connection.checkSqlCode(code)
        val result = when (code) {
            SQLITE_DONE -> SQLiteResultSet(this, true)
            SQLITE_ROW -> SQLiteResultSet(this, false)
            else -> throw IllegalStateException()
        }
        openedResultSetCount++
        return result
    }

    override fun executeUpdate(): Long {
        val code = sqlite3_step(stmt)
        connection.checkSqlCode(code)
        val rownum = sqlite3_column_int64(stmt, 0)
        sqlite3_reset(stmt)
        return rownum
    }

    override fun setNull(index: Int) {
        connection.checkSqlCode(sqlite3_bind_null(stmt, index + 1))
    }

    override fun close() {
        if (openedResultSetCount > 0)
            throw SQLException("Not all ResultSet closed")
        sqlite3_clear_bindings(stmt)
        sqlite3_finalize(stmt)
        free(native)
    }
}