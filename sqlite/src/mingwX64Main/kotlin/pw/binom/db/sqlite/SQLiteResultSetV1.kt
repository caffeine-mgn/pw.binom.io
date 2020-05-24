package pw.binom.db.sqlite

import pw.binom.db.ResultSet
import pw.binom.db.SQLException
/*
class SQLiteResultSetV1 : ResultSet {

    private class Record(val values: Array<String?>, var next: Record?)

    private var ready = false
    private var ended = false
    var size = 0
        private set

    internal fun updateFinish() {
        ready = true
        ended = first == null
    }

    private var current: Record? = null
    private var last: Record? = null
    private var first: Record? = null

    internal var columnsInserted = false
    internal var columns1 = ArrayList<String>()

    internal fun addRecord(values: Array<String?>) {
        val r = Record(values, null)
        last?.next = r
        last = r
        if (first == null) {
            first = r
        }
        size++
    }

    override val columns: List<String>
        get() = columns1

    override fun next(): Boolean {
        if (ended)
            return false
        current = if (current == null) {
            first
        } else {
            current?.next
        }

        if (current == null) {
            ended = true
            return false
        }
        return true
    }

    private fun getActiveRecord() = current ?: throw SQLException("No Active Record")
    private inline fun checkRange(index: Int) {
        if (index < 0 || index >= columns1.size)
            throw ArrayIndexOutOfBoundsException()
    }

    private fun nullNotExpected(): Nothing = throw SQLException("Column value is null")

    override fun getString(index: Int): String {
        checkRange(index)
        val record = getActiveRecord()
        return record.values[index] ?: nullNotExpected()
    }

    override fun getString(column: String): String {
        val index = columns1.indexOf(column)
        if (index < 0)
            throw SQLException("Column \"$column\" not found")
        val current = getActiveRecord()
        return current.values[index] ?: nullNotExpected()
    }

    override fun getBoolean(index: Int): Boolean =
            getInt(index) > 0

    override fun getBoolean(column: String): Boolean =
            getInt(column) > 0

    override fun getInt(index: Int): Int {
        val str = getString(index)
        return str.toIntOrNull() ?: throw SQLException("Can't convert \"$str\" to int")
    }

    override fun getInt(column: String): Int {
        val str = getString(column)
        return str.toIntOrNull() ?: throw SQLException("Can't convert \"$str\" to int")
    }

    override fun getLong(index: Int): Long {
        val str = getString(index)
        return str.toLongOrNull() ?: throw SQLException("Can't convert \"$str\" to long")
    }

    override fun getLong(column: String): Long {
        val str = getString(column)
        return str.toLongOrNull() ?: throw SQLException("Can't convert \"$str\" to long")
    }

    override fun getFloat(index: Int): Float {
        val str = getString(index)
        return str.toFloatOrNull() ?: throw SQLException("Can't convert \"$str\" to float")
    }

    override fun getFloat(column: String): Float {
        val str = getString(column)
        return str.toFloatOrNull() ?: throw SQLException("Can't convert \"$str\" to float")
    }

    override fun getBlob(index: Int): ByteArray {
        checkRange(index)
        val data = getActiveRecord().values[index] ?: nullNotExpected()
        val v = data.toCharArray()
        println("getBlob($index)=${data} ${data.length}")
        return ByteArray(v.size) { v[it].toByte() }
    }

    override fun getBlob(column: String): ByteArray {
        val index = columns1.indexOf(column)
        if (index < 0)
            throw SQLException("Column \"$column\" not found")
        return getBlob(index)
    }

    override fun isNull(index: Int): Boolean {
        checkRange(index)
        return getActiveRecord().values[index] == null
    }

    override fun isNull(column: String): Boolean {
        val index = columns1.indexOf(column)
        if (index < 0)
            throw SQLException("Column \"$column\" not found")
        val current = getActiveRecord()
        return current.values[index] == null
    }

    override fun close() {
        columns1.clear()
    }
}
*/