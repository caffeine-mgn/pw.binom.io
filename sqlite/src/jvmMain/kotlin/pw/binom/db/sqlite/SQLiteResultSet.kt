package pw.binom.db.sqlite

import java.sql.ResultSet

class SQLiteResultSet(private val native: ResultSet) : pw.binom.db.ResultSet {
    override val columns: List<String> by lazy {
        val count = native.metaData.columnCount
        val out = ArrayList<String>(count)
        (0 until count).forEach {
            native.metaData.getColumnName(it + 1)
        }
        out
    }

    override fun next() = native.next()

    override fun getString(index: Int): String = native.getString(index + 1)
    override fun getString(column: String): String = native.getString(column)

    override fun getBoolean(index: Int) = native.getBoolean(index + 1)
    override fun getBoolean(column: String): Boolean = native.getBoolean(column)

    override fun getInt(index: Int) = native.getInt(index + 1)
    override fun getInt(column: String): Int = native.getInt(column)

    override fun getLong(index: Int) = native.getLong(index + 1)
    override fun getLong(column: String): Long = native.getLong(column)

    override fun getFloat(index: Int) = native.getFloat(index + 1)
    override fun getFloat(column: String): Float = native.getFloat(column)

    override fun isNull(index: Int) =
            native.getObject(index + 1) != null

    override fun isNull(column: String): Boolean =
            native.getObject(column) != null

    override fun close() {
        native.close()
    }
}