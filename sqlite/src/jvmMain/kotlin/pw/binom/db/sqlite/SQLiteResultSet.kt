package pw.binom.db.sqlite

import pw.binom.db.SQLException
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
    private fun nullNotExpected(): Nothing = throw SQLException("Column value is null")
    override fun getString(index: Int): String = native.getString(index + 1)?:nullNotExpected()
    override fun getString(column: String): String = native.getString(column)?:nullNotExpected()

    override fun getBoolean(index: Int) = native.getBoolean(index + 1)?:nullNotExpected()
    override fun getBoolean(column: String): Boolean = native.getBoolean(column)?:nullNotExpected()

    override fun getInt(index: Int) = native.getInt(index + 1)?:nullNotExpected()
    override fun getInt(column: String): Int = native.getInt(column)?:nullNotExpected()

    override fun getLong(index: Int) = native.getLong(index + 1)?:nullNotExpected()
    override fun getLong(column: String): Long = native.getLong(column)?:nullNotExpected()

    override fun getFloat(index: Int) = native.getFloat(index + 1)?:nullNotExpected()
    override fun getFloat(column: String): Float = native.getFloat(column)?:nullNotExpected()
    override fun getBlob(index: Int): ByteArray {
        return (native.getBinaryStream(index + 1)?:nullNotExpected()).readAllBytes()
//        val b = native.getBlob(index + 1)
//        val buf = ByteArray(b.length().toInt())
//        b.getBytes(0, buf.size)
//        return buf
    }

    override fun getBlob(column: String): ByteArray {
        val b = native.getBlob(column)?:nullNotExpected()
        val buf = ByteArray(b.length().toInt())
        b.getBytes(0, buf.size)
        return buf
    }

    override fun isNull(index: Int) =
            native.getObject(index + 1) == null

    override fun isNull(column: String): Boolean =
            native.getObject(column) == null

    override fun close() {
        native.close()
    }
}