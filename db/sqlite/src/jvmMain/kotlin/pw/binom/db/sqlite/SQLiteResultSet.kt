package pw.binom.db.sqlite

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.date.Date
import pw.binom.db.SyncResultSet
import java.sql.ResultSet

class SQLiteResultSet(private val native: ResultSet) : SyncResultSet {

    override val columns: List<String> by lazy {
        val count = native.metaData.columnCount
        val out = ArrayList<String>(count)
        (0 until count).forEach {
            native.metaData.getColumnName(it + 1)
        }
        out
    }

    override fun next() = native.next()
    override fun getString(index: Int): String? {
        native.getObject(index) ?: return null
        return native.getString(index + 1)
    }

    override fun getString(column: String): String? {
        native.getObject(column) ?: return null
        return native.getString(column)
    }

    override fun getBoolean(index: Int): Boolean? = getInt(index + 1)?.let { it > 0 }
    override fun getBoolean(column: String): Boolean? = getInt(column)?.let { it > 0 }

    override fun getInt(index: Int): Int? {
        native.getObject(index) ?: return null
        return native.getInt(index + 1)
    }

    override fun getInt(column: String): Int? {
        native.getObject(column) ?: return null
        return native.getInt(column)
    }

    override fun getLong(index: Int): Long? {
        native.getObject(index) ?: return null
        return native.getLong(index + 1)
    }

    override fun getLong(column: String): Long? {
        native.getObject(column) ?: return null
        return native.getLong(column)
    }

    override fun getFloat(index: Int): Float? {
        native.getObject(index) ?: return null
        return native.getFloat(index + 1)
    }

    override fun getFloat(column: String): Float? {
        native.getObject(column) ?: return null
        return native.getFloat(column)
    }

    override fun getBigDecimal(index: Int): BigDecimal? {
        TODO("Not yet implemented")
    }

    override fun getBigDecimal(column: String): BigDecimal? {
        TODO("Not yet implemented")
    }

    override fun getDouble(index: Int): Double? {
        native.getObject(index) ?: return null
        return native.getDouble(index)
    }

    override fun getDouble(column: String): Double? {
        native.getObject(column) ?: return null
        return native.getDouble(column)
    }

    override fun getBlob(index: Int): ByteArray? {
        val stream = native.getBinaryStream(index + 1) ?: return null
        return stream.readAllBytes()
    }

    override fun getBlob(column: String): ByteArray? {
        val b = native.getBlob(column) ?: return null
        val buf = ByteArray(b.length().toInt())
        b.getBytes(0, buf.size)
        return buf
    }

    override fun isNull(index: Int) =
        native.getObject(index + 1) == null

    override fun isNull(column: String): Boolean =
        native.getObject(column) == null

    override fun getDate(index: Int): Date? {
        native.getObject(index) ?: return null
        return Date(native.getTimestamp(index).time)
    }

    override fun getDate(column: String): Date? {
        native.getObject(column) ?: return null
        return Date(native.getTimestamp(column).time)
    }

    override fun close() {
        native.close()
    }
}