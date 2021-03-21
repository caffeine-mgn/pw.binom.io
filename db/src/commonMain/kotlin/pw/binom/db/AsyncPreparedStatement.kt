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
    suspend fun executeQuery(): AsyncResultSet
    suspend fun executeUpdate():Long
    fun set(index: Int, value: UUID) {
        val buf = ByteArray(16)
        set(index, value.toByteArray(buf))
    }
}