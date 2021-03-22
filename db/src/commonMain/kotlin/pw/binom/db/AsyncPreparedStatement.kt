package pw.binom.db

import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable

interface AsyncPreparedStatement : AsyncCloseable {
    val connection: AsyncConnection
    fun set(index: Int, value: Float)
    fun set(index: Int, value: Int)
    fun set(index: Int, value: Long)
    fun set(index: Int, value: String)
    fun set(index: Int, value: Boolean)
    fun set(index: Int, value: ByteArray)
    fun set(index: Int, value: Date)
    fun setNull(index: Int)
    fun setValue(index: Int, value: Any?) {
        when (value) {
            null -> setNull(index)
            is Float -> set(index, value)
            is Int -> set(index, value)
            is Long -> set(index, value)
            is String -> set(index, value)
            is Boolean -> set(index, value)
            is ByteArray -> set(index, value)
            is Date -> set(index, value)
            is UUID -> set(index, value)
        }
    }

    suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return executeQuery()
    }

    suspend fun executeUpdate(vararg arguments: Any?): Long {
        arguments.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return executeUpdate()
    }

    suspend fun executeQuery(): AsyncResultSet
    suspend fun executeUpdate(): Long
    fun set(index: Int, value: UUID) {
        val buf = ByteArray(16)
        set(index, value.toByteArray(buf))
    }
}