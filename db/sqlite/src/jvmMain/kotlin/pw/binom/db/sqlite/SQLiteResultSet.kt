package pw.binom.db.sqlite

import pw.binom.collections.defaultMutableList
import pw.binom.date.DateTime
import pw.binom.db.ColumnType
import pw.binom.db.sync.SyncResultSet
import java.sql.ResultSet
import java.sql.Types

class SQLiteResultSet(private val native: ResultSet) : SyncResultSet {

    override val columns: List<String> by lazy {
        val count = native.metaData.columnCount
        val out = defaultMutableList<String>(count)
        (0 until count).forEach {
            out += native.metaData.getColumnName(it + 1)
        }
        out
    }

    override fun next() = native.next()
    override fun getString(index: Int): String? {
        native.getObject(index + 1) ?: return null
        return native.getString(index + 1)
    }

    override fun getString(column: String): String? {
        native.getObject(column) ?: return null
        return native.getString(column)
    }

    override fun getBoolean(index: Int): Boolean? = getInt(index + 1)?.let { it > 0 }
    override fun getBoolean(column: String): Boolean? = getInt(column)?.let { it > 0 }

    override fun getInt(index: Int): Int? {
        native.getObject(index + 1) ?: return null
        return native.getInt(index + 1)
    }

    override fun getInt(column: String): Int? {
        native.getObject(column) ?: return null
        return native.getInt(column)
    }

    override fun getLong(index: Int): Long? {
        native.getObject(index + 1) ?: return null
        return native.getLong(index + 1)
    }

    override fun getLong(column: String): Long? {
        native.getObject(column) ?: return null
        return native.getLong(column)
    }

    override fun getFloat(index: Int): Float? {
        native.getObject(index + 1) ?: return null
        return native.getFloat(index + 1)
    }

    override fun getFloat(column: String): Float? {
        native.getObject(column) ?: return null
        return native.getFloat(column)
    }

//    override fun getBigDecimal(index: Int): BigDecimal? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getBigDecimal(column: String): BigDecimal? {
//        TODO("Not yet implemented")
//    }

    override fun getDouble(index: Int): Double? {
        native.getObject(index + 1) ?: return null
        return native.getDouble(index + 1)
    }

    override fun getDouble(column: String): Double? {
        native.getObject(column) ?: return null
        return native.getDouble(column)
    }

    override fun getBlob(index: Int): ByteArray? {
        return native.getObject(index + 1) as ByteArray?
//        val stream = native.getBinaryStream(index + 1) ?: return null
//        return stream.readAllBytes()
    }

    override fun getBlob(column: String): ByteArray? {
        return native.getObject(column) as ByteArray?
//        val b = native.getBlob(column) ?: return null
//        val buf = ByteArray(b.length().toInt())
//        b.getBytes(0, buf.size)
//        return buf
    }

    override fun isNull(index: Int) =
        native.getObject(index + 1) == null

    override fun isNull(column: String): Boolean =
        native.getObject(column) == null

    override fun getDate(index: Int): DateTime? {
        native.getObject(index + 1) ?: return null
        return DateTime(native.getLong(index + 1))
    }

    override fun getDate(column: String): DateTime? {
        native.getObject(column) ?: return null
        return DateTime(native.getLong(column))
    }

    override fun columnIndex(column: String): Int =
        native.findColumn(column)

    override fun columnType(index: Int): ColumnType =
        when (val type = native.metaData.getColumnType(index + 1)) {
            Types.BIT -> ColumnType.BIT
            Types.TINYINT -> ColumnType.TINYINT
            Types.SMALLINT -> ColumnType.SMALLINT
            Types.INTEGER -> ColumnType.INTEGER
            Types.BIGINT -> ColumnType.BIGINT
            Types.FLOAT -> ColumnType.FLOAT
            Types.REAL -> ColumnType.REAL
            Types.DOUBLE -> ColumnType.DOUBLE
            Types.NUMERIC -> ColumnType.NUMERIC
            Types.DECIMAL -> ColumnType.DECIMAL
            Types.CHAR -> ColumnType.CHAR
            Types.VARCHAR -> ColumnType.VARCHAR
            Types.LONGVARCHAR -> ColumnType.LONGVARCHAR
            Types.DATE -> ColumnType.DATE
            Types.TIME -> ColumnType.TIME
            Types.TIMESTAMP -> ColumnType.TIMESTAMP
            Types.BINARY -> ColumnType.BINARY
            Types.VARBINARY -> ColumnType.VARBINARY
            Types.LONGVARBINARY -> ColumnType.LONGVARBINARY
            Types.NULL -> ColumnType.NULL
            Types.OTHER -> ColumnType.OTHER
            Types.BLOB -> ColumnType.BINARY
            Types.CLOB -> ColumnType.VARCHAR
            else -> error("Unknown data type $type")
        }

    override fun columnType(column: String): ColumnType = columnType(columnIndex(column))

    override fun close() {
        native.close()
    }
}
