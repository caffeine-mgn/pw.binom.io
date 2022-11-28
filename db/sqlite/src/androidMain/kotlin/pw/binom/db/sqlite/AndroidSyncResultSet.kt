package pw.binom.db.sqlite

import android.database.Cursor
import pw.binom.date.DateTime
import pw.binom.db.ColumnType
import pw.binom.db.sync.SyncResultSet

class AndroidSyncResultSet(val native: Cursor) : SyncResultSet {
    override fun next(): Boolean = native.moveToNext()

    override val columns: List<String> by lazy { native.columnNames.toList() }

    override fun columnType(index: Int): ColumnType =
        when (val type = native.getType(index + 1)) {
//            Types.TINYINT -> ColumnType.TINYINT
//            Types.SMALLINT -> ColumnType.SMALLINT
            Cursor.FIELD_TYPE_INTEGER -> ColumnType.INTEGER
//            Types.BIGINT -> ColumnType.BIGINT
//            Types.FLOAT -> ColumnType.FLOAT
//            Types.REAL -> ColumnType.REAL
            Cursor.FIELD_TYPE_FLOAT -> ColumnType.DOUBLE
//            Types.NUMERIC -> ColumnType.NUMERIC
//            Types.DECIMAL -> ColumnType.DECIMAL
//            Types.CHAR -> ColumnType.CHAR
//            Types.VARCHAR -> ColumnType.VARCHAR
//            Types.LONGVARCHAR -> ColumnType.LONGVARCHAR
//            Types.DATE -> ColumnType.DATE
//            Types.TIME -> ColumnType.TIME
//            Types.TIMESTAMP -> ColumnType.TIMESTAMP
//            Types.BINARY -> ColumnType.BINARY
//            Types.VARBINARY -> ColumnType.VARBINARY
//            Types.LONGVARBINARY -> ColumnType.LONGVARBINARY
            Cursor.FIELD_TYPE_NULL -> ColumnType.NULL
//            Types.OTHER -> ColumnType.OTHER
            Cursor.FIELD_TYPE_BLOB -> ColumnType.BINARY
            Cursor.FIELD_TYPE_STRING -> ColumnType.VARCHAR
            else -> error("Unknown data type $type")
        }

    override fun columnType(column: String): ColumnType =
        columnType(native.getColumnIndex(column))

    override fun getString(index: Int): String? = if (native.isNull(index)) {
        null
    } else {
        native.getString(index)
    }

    override fun getString(column: String): String? = getString(native.getColumnIndex(column))

    override fun getBoolean(index: Int): Boolean? = getInt(index)?.let { it > 0 }

    override fun getBoolean(column: String): Boolean? =
        getBoolean(native.getColumnIndex(column))

    override fun getInt(index: Int): Int? =
        if (native.isNull(index)) {
            null
        } else {
            native.getInt(index)
        }

    override fun getInt(column: String): Int? =
        getInt(native.getColumnIndex(column))

    override fun getLong(index: Int): Long? =
        if (native.isNull(index)) {
            null
        } else {
            native.getLong(index)
        }

    override fun getLong(column: String): Long? =
        getLong(native.getColumnIndex(column))

    override fun getDouble(index: Int): Double? =
        if (native.isNull(index)) {
            null
        } else {
            native.getDouble(index)
        }

    override fun getDouble(column: String): Double? =
        getDouble(native.getColumnIndex(column))

    override fun getBlob(index: Int): ByteArray? =
        if (native.isNull(index)) {
            null
        } else {
            native.getBlob(index)
        }

    override fun getBlob(column: String): ByteArray? =
        getBlob(native.getColumnIndex(column))

    override fun isNull(index: Int): Boolean = native.isNull(index)

    override fun isNull(column: String): Boolean =
        isNull(native.getColumnIndex(column))

    override fun getDate(index: Int): DateTime? =
        getLong(index)?.let { DateTime(it) }

    override fun getDate(column: String): DateTime? =
        getDate(native.getColumnIndex(column))

    override fun close() {
        native.close()
    }
}
