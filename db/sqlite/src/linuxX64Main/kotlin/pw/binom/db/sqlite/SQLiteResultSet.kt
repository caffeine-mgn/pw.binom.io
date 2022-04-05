package pw.binom.db.sqlite

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import pw.binom.date.Date
import pw.binom.db.SQLException
import pw.binom.db.sync.SyncResultSet

class SQLiteResultSet(
    val prepareStatement: SQLitePrepareStatement,
    var empty: Boolean,
) : SyncResultSet {
    private var columnCount = 0
    private val columnsMap = HashMap<String, Int>()

    override lateinit var columns: List<String>

    init {
        columnCount = sqlite3_column_count(stmt)
        val out = ArrayList<String>(columnCount)
        (0 until columnCount).forEach {
            val name = sqlite3_column_origin_name(stmt, it)!!
                .reinterpret<ByteVar>()
                .toKStringFromUtf8()
            out += name
            columnsMap[name] = it
        }
        columns = out
    }

    private var skipOne = true

    override fun next(): Boolean {
        if (empty)
            return false

        if (skipOne) {
            skipOne = false
            return true
        }

        val bb = when (sqlite3_step(stmt)) {
            SQLITE_BUSY -> throw SQLException("Database is Busy")
            SQLITE_DONE -> {
                empty = true
                false
            }
            SQLITE_ROW -> {
                columnCount = sqlite3_column_count(stmt)
                true
            }
            SQLITE_ERROR -> throw SQLException(
                sqlite3_errmsg(prepareStatement.connection.ctx.pointed!!.value)?.toKStringFromUtf8()
                    ?: "Unknown Error"
            )
            SQLITE_MISUSE -> throw SQLException("Database is Misuse")
            else -> throw SQLException()
        }
        return bb
    }

    private inline fun checkRange(index: Int) {
        if (index < 0 || index >= columnCount)
            throw ArrayIndexOutOfBoundsException()
    }

    override fun getString(index: Int): String? {
        checkRange(index)
        return sqlite3_column_text(stmt, index)
            ?.reinterpret<ByteVar>()
            ?.toKStringFromUtf8()
    }

    override fun getBlob(index: Int): ByteArray? {
        if (isNullColumn(index)) {
            return null
        }
        val len = sqlite3_column_bytes(stmt, index)
        return sqlite3_column_blob(stmt, index)!!.readBytes(len)
    }

    override fun getBlob(column: String): ByteArray? = getBlob(columnIndex(column))

    override fun getString(column: String): String? =
        getString(columnIndex(column))

    override fun getBoolean(index: Int): Boolean? =
        getInt(index)?.let { it > 0 }

    override fun getBoolean(column: String): Boolean? =
        getInt(column)?.let { it > 0 }

    override fun getInt(index: Int): Int? {
        checkRange(index)
        if (isNullColumn(index)) {
            return null
        }
        return sqlite3_column_int(stmt, index)
    }

    override fun getInt(column: String): Int? =
        getInt(columnIndex(column))

    override fun getLong(index: Int): Long? {
        checkRange(index)
        if (isNullColumn(index)) {
            return null
        }
        sqlite3_column_type(stmt, index)
        return sqlite3_column_int64(stmt, index)
    }

    override fun getLong(column: String): Long? =
        getLong(columnIndex(column))

    override fun getFloat(index: Int): Float? {
        return getDouble(index)?.toFloat()
    }

    override fun getFloat(column: String): Float? =
        getFloat(columnIndex(column))

//    override fun getBigDecimal(index: Int): BigDecimal? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getBigDecimal(column: String): BigDecimal? {
//        TODO("Not yet implemented")
//    }

    override fun getDouble(index: Int): Double? {
        checkRange(index)
        if (isNullColumn(index)) {
            return null
        }
        return sqlite3_column_double(stmt, index)
    }

    override fun getDouble(column: String): Double? =
        getDouble(columnIndex(column))

    override fun columnIndex(name: String) =
        columnsMap[name] ?: throw SQLException("Column \"$name\" not found")

    private inline val stmt
        get() = prepareStatement.native[0]

    override fun isNull(index: Int): Boolean {
        checkRange(index)
        return isNullColumn(index)
    }

    private inline fun isNullColumn(index: Int) =
        sqlite3_column_type(stmt, index) == SQLITE_NULL

    override fun isNull(column: String) =
        isNullColumn(columnIndex(column))

    override fun getDate(index: Int): Date? =
        getLong(index)?.let { Date(it) }

    override fun getDate(column: String): Date? =
        getDate(columnIndex(column))

    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw SQLException("ResultSet already closed")
    }

    override fun close() {
        checkClosed()
        prepareStatement.openedResultSetCount--
        sqlite3_clear_bindings(stmt)
        sqlite3_reset(stmt)
        closed = true
    }
}
