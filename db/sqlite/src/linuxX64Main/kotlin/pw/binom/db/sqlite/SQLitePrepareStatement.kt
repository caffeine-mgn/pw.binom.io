package pw.binom.db.sqlite

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
// import com.ionspin.kotlin.bignum.integer.BigInteger
import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.date.DateTime
import pw.binom.db.SQLException
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncResultSet
import pw.binom.io.ClosedException

class SQLitePrepareStatement(
    override val connection: SQLiteConnector,
    internal val native: CPointer<CPointerVar<sqlite3_stmt>>
) : SyncPreparedStatement {

    internal var openedResultSetCount = 0
    private val closed = AtomicBoolean(false)

    private fun checkClosed() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    private inline val stmt
        get() = native[0]

    private inline val maxParams
        get() = sqlite3_limit(connection.ctx.pointed.value, SQLITE_LIMIT_VARIABLE_NUMBER, -1)

    private inline fun checkRange(index: Int) {
        if (index < 0 || index >= maxParams) {
            throw ArrayIndexOutOfBoundsException()
        }
    }

//    override fun set(index: Int, value: BigInteger) {
//        checkClosed()
//        checkRange(index)
//        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value.toString(), -1, null))
//    }
//
//    override fun set(index: Int, value: BigDecimal) {
//        checkClosed()
//        checkRange(index)
//        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value.toString(), -1, null))
//    }

    override fun set(index: Int, value: Double) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_double(stmt, index + 1, value))
    }

    override fun set(index: Int, value: Float) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_double(stmt, index + 1, value.toDouble()))
    }

    override fun set(index: Int, value: Int) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_int(stmt, index + 1, value))
    }

    override fun set(index: Int, value: Long) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_int64(stmt, index + 1, value))
    }

    override fun set(index: Int, value: String) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value, -1, null))
    }

    override fun set(index: Int, value: Boolean) {
        checkClosed()
        set(index, if (value) 1 else 0)
    }

    override fun set(index: Int, value: ByteArray) {
        checkClosed()
        checkRange(index)
        connection.checkSqlCode(sqlite3_bind_blob(stmt, index + 1, value.refTo(0), value.size, SQLITE_STATIC))
    }

    override fun set(index: Int, value: DateTime) {
        checkClosed()
        set(index, value.time)
    }

    override fun executeQuery(): SyncResultSet {
        checkClosed()
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
        checkClosed()
        val before = sqlite3_total_changes(connection.ctx.pointed.value)
        val code = sqlite3_step(stmt)
        connection.checkSqlCode(code)
        val rownum = sqlite3_column_int64(stmt, 0)
        sqlite3_reset(stmt)
        return (sqlite3_total_changes(connection.ctx.pointed.value) - before).toLong()
    }

    override fun setNull(index: Int) {
        checkClosed()
        connection.checkSqlCode(sqlite3_bind_null(stmt, index + 1))
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        println("Closing SQLite Prepared Statement")
        connection.delete(this)
        if (openedResultSetCount > 0) {
            throw SQLException("Not all ResultSet closed")
        }
        sqlite3_clear_bindings(stmt)
        sqlite3_finalize(stmt)
        nativeHeap.free(native)
    }
}
