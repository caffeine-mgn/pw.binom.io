package pw.binom.db

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
    fun isNull(index: Int): Boolean

    fun getString(column: String): String
    fun getBoolean(column: String): Boolean
    fun getInt(column: String): Int
    fun getLong(column: String): Long
    fun getFloat(column: String): Float
    fun isNull(column: String): Boolean

    class InvalidColumnTypeException : IOException()

    enum class ColumnType {
        STRING, BOOLEAN, INT, LONG, FLOAT
    }
}