package pw.binom.db

import pw.binom.UUID
import pw.binom.io.Closeable
import pw.binom.io.IOException

interface ResultSet {

    val columns: List<String>
    fun getString(index: Int): String?
    fun getBoolean(index: Int): Boolean?
    fun getInt(index: Int): Int?
    fun getLong(index: Int): Long?
    fun getFloat(index: Int): Float?
    fun getBlob(index: Int): ByteArray?
    fun isNull(index: Int): Boolean
    fun getUUID(index: Int) = getBlob(index)?.let { UUID.create(it) }

    fun getString(column: String): String?
    fun getBoolean(column: String): Boolean?
    fun getInt(column: String): Int?
    fun getLong(column: String): Long?
    fun getFloat(column: String): Float?
    fun getBlob(column: String): ByteArray?
    fun isNull(column: String): Boolean
    fun getUUID(column: String) = getBlob(column)?.let { UUID.create(it) }

    class InvalidColumnTypeException : IOException()

    enum class ColumnType {
        STRING, BOOLEAN, INT, LONG, FLOAT, DOUBLE, UUID
    }
}