package pw.binom.db.serialization

import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.date.parseIso8601Date
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncResultSet

abstract class AbstractStaticSyncResultSet<T> : AsyncResultSet {
    protected abstract val list: List<T>
    private var index = -1
    override suspend fun next(): Boolean {
        index++
        return index > 0 && index < list.size
    }

    private fun getIndex(column: String): Int {
        val p = columns.indexOfFirst { it.lowercase() == column.lowercase() }
        if (p == -1) {
            throw IllegalStateException("Column \"$column\" not found")
        }
        return p
    }

    protected abstract fun getString(index: Int, value: T): String?
    protected open fun getBoolean(index: Int, value: T): Boolean? =
        getString(index, value)?.let { it == "t" || it == "true" || it == "1" }

    protected open fun getLong(index: Int, value: T): Long? =
        getString(index, value)?.toLong()

    protected open fun getInt(index: Int, value: T): Int? =
        getString(index, value)?.toInt()

    protected open fun getFloat(index: Int, value: T): Float? =
        getString(index, value)?.toFloat()

    protected open fun getDouble(index: Int, value: T): Double? =
        getString(index, value)?.toDouble()

    protected open fun getDate(index: Int, value: T): Date? =
        getString(index, value)?.parseIso8601Date()

//    protected open fun getBigDecimal(index: Int, value: T): BigDecimal? =
//        getString(index, value)?.let { BigDecimal.parseString(it) }

    protected abstract fun getBlob(index: Int, value: T): ByteArray?
    protected abstract fun isNull(index: Int, value: T): Boolean
    protected open fun getUUID(index: Int, value: T): UUID? =
        getBlob(index, value)?.let { UUID.create(it) }

    private fun getElement(): T {
        if (index == -1) {
            throw SQLException("Result not ready. Try call \"fun next():Boolean\"")
        }
        if (index >= list.size) {
            throw SQLException("Result is finished")
        }
        return list[index]
    }

    override fun getString(index: Int): String? =
        getElement()?.let { getString(index, it) }

    override fun getFloat(index: Int): Float? =
        getElement()?.let { getFloat(index, it) }

    override fun getFloat(column: String): Float? =
        getFloat(columnIndex(column))

    override fun getUUID(index: Int): UUID? =
        getElement()?.let { getUUID(index, it) }

    override fun getUUID(column: String): UUID? =
        getUUID(columnIndex(column))

    override fun getString(column: String): String? =
        getString(columnIndex(column))

    override fun getBoolean(index: Int): Boolean? =
        getElement()?.let { getBoolean(index, it) }

    override fun getBoolean(column: String): Boolean? =
        getBoolean(columnIndex(column))

    override fun getInt(index: Int): Int? =
        getElement()?.let { getInt(index, it) }

    override fun getInt(column: String): Int? =
        getInt(columnIndex(column))

    override fun getLong(index: Int): Long? =
        getElement()?.let { getLong(index, it) }

    override fun getLong(column: String): Long? =
        getLong(columnIndex(column))

//    override fun getBigDecimal(index: Int): BigDecimal? =
//        getElement()?.let { getBigDecimal(index, it) }

//    override fun getBigDecimal(column: String): BigDecimal? =
//        getBigDecimal(columnIndex(column))

    override fun getDouble(index: Int): Double? =
        getElement()?.let { getDouble(index, it) }

    override fun getDouble(column: String): Double? =
        getDouble(columnIndex(column))

    override fun getBlob(index: Int): ByteArray? =
        getElement()?.let { getBlob(index, it) }

    override fun getBlob(column: String): ByteArray? =
        getBlob(columnIndex(column))

    override fun isNull(index: Int): Boolean =
        getElement()!!.let { isNull(index, it) }

    override fun isNull(column: String): Boolean =
        isNull(columnIndex(column))

    override fun getDate(index: Int): Date? =
        getElement()?.let { getDate(index, it) }

    override fun getDate(column: String): Date? =
        getDate(columnIndex(column))

    override suspend fun asyncClose() {
        // NOP
    }
}
