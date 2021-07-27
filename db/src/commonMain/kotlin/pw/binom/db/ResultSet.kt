package pw.binom.db

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.io.Closeable
import pw.binom.io.IOException

interface ResultSet {

    val columns: List<String>
    fun getString(index: Int): String?
    fun getBoolean(index: Int): Boolean?
    fun getInt(index: Int): Int?
    fun getLong(index: Int): Long?
    fun getFloat(index: Int): Float? =
        getDouble(index)?.toFloat()

    fun getBigDecimal(index:Int):BigDecimal?
    fun getBigDecimal(column:String):BigDecimal?

    fun getDouble(index: Int): Double?
    fun getBlob(index: Int): ByteArray?
    fun isNull(index: Int): Boolean
    fun getUUID(index: Int) = getBlob(index)?.let { UUID.create(it) }
    fun getDate(index: Int): Date?

    fun getString(column: String): String?
    fun getBoolean(column: String): Boolean?
    fun getInt(column: String): Int?
    fun getLong(column: String): Long?
    fun getFloat(column: String): Float? =
        getDouble(column)?.toFloat()

    fun getDouble(column: String): Double?
    fun getBlob(column: String): ByteArray?
    fun isNull(column: String): Boolean
    fun getUUID(column: String) = getBlob(column)?.let { UUID.create(it) }
    fun getDate(column: String): Date?
    fun columnIndex(column:String):Int{
        val p = columns.indexOfFirst { it.lowercase() == column.lowercase() }
        if (p == -1) {
            throw IllegalStateException("Column \"$column\" not found")
        }
        return p
    }

    class InvalidColumnTypeException : IOException()

    enum class ColumnType {
        STRING, BOOLEAN, INT, LONG, FLOAT, DOUBLE, UUID
    }
}