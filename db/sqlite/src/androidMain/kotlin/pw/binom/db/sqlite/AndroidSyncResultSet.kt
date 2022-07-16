package pw.binom.db.sqlite

import android.database.Cursor
import pw.binom.date.DateTime
import pw.binom.db.sync.SyncResultSet

class AndroidSyncResultSet(val native: Cursor) : SyncResultSet {
    override fun next(): Boolean = native.moveToNext()

    override val columns: List<String> by lazy { native.columnNames.toList() }

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
