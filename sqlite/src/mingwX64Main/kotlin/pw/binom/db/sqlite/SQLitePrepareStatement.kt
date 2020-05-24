package pw.binom.db.sqlite

import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import platform.posix.free
import pw.binom.db.PreparedStatement
import pw.binom.db.ResultSet
import pw.binom.db.SQLException

class SQLitePrepareStatement(override val connection: SQLiteConnector,
                             internal val native: CPointer<CPointerVar<sqlite3_stmt>>
) : PreparedStatement {

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
        sqlite3_bind_double(stmt, index + 1, value.toDouble())
    }

    override fun set(index: Int, value: Int) {
        checkRange(index)
        sqlite3_bind_int(stmt, index + 1, value)
    }

    override fun set(index: Int, value: Long) {
        checkRange(index)
        sqlite3_bind_int64(stmt, index + 1, value)
    }

    override fun set(index: Int, value: String) {
        checkRange(index)
        sqlite3_bind_text(stmt, index + 1, value, -1, null)
    }

    override fun set(index: Int, value: Boolean) {
        checkRange(index)
        set(index, if (value) 1 else 0)
    }

    override fun executeQuery(): ResultSet {
        val result = when (sqlite3_step(stmt)) {
            SQLITE_BUSY -> throw SQLException("Database is Busy")
            SQLITE_DONE -> SQLiteResultSetV2(this, true)
            SQLITE_ROW -> SQLiteResultSetV2(this, false)
            SQLITE_ERROR -> throw SQLException(
                    sqlite3_errmsg(connection.ctx.pointed.value)?.toKStringFromUtf8() ?: "Unknown Error"
            )
            SQLITE_MISUSE -> throw SQLException("Database is Misuse")
            else -> throw SQLException()
        }
        openedResultSetCount++
        return result
    }

    override fun executeUpdate(query: String) {
        when (sqlite3_step(stmt)) {
            SQLITE_BUSY -> throw SQLException("Database is Busy")
            SQLITE_DONE, SQLITE_ROW -> {
                sqlite3_reset(stmt)
            }
            SQLITE_ERROR -> throw SQLException(
                    sqlite3_errmsg(connection.ctx.pointed.value)?.toKStringFromUtf8() ?: "Unknown Error"
            )
            SQLITE_MISUSE -> throw SQLException("Database is Misuse")
            else -> throw SQLException()
        }
    }

    override fun close() {
        if (openedResultSetCount > 0)
            throw SQLException("Not all ResultSet closed")
        sqlite3_clear_bindings(stmt)
        sqlite3_finalize(stmt)
        free(native)
    }
}