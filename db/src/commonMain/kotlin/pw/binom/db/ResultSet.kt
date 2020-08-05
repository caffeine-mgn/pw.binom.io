package pw.binom.db

import pw.binom.UUID
import pw.binom.io.Closeable
import pw.binom.io.IOException

interface ResultSet : Closeable {
    val columns: List<String>
    fun next(): Boolean
    fun getString(index: Int): String
    fun getBoolean(index: Int): Boolean
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getFloat(index: Int): Float
    fun getBlob(index: Int): ByteArray
    fun isNull(index: Int): Boolean

    fun getString(column: String): String
    fun getBoolean(column: String): Boolean
    fun getInt(column: String): Int
    fun getLong(column: String): Long
    fun getFloat(column: String): Float
    fun getBlob(column: String): ByteArray
    fun isNull(column: String): Boolean
    fun getUUID(index: Int) = UUID.create(getBlob(index))
    fun getUUID(column: String) = UUID.create(getBlob(column))

    fun <T> map(func: (ResultSet) -> T): Iterator<T> = object : Iterator<T> {
        private var end = this@ResultSet.next()
        override fun hasNext(): Boolean = end

        override fun next(): T {
            val r = func(this@ResultSet)
            end = this@ResultSet.next()
            return r
        }
    }

    class InvalidColumnTypeException : IOException()

    enum class ColumnType {
        STRING, BOOLEAN, INT, LONG, FLOAT
    }
}