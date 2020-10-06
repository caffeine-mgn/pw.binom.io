package pw.binom.db

import pw.binom.UUID
import pw.binom.io.Closeable

interface PreparedStatement : Closeable {
    val connection: Connection
    fun set(index: Int, value: Float)
    fun set(index: Int, value: Int)
    fun set(index: Int, value: Long)
    fun set(index: Int, value: String)
    fun set(index: Int, value: Boolean)
    fun set(index: Int, value: ByteArray)
    fun setNull(index: Int)
    fun executeQuery(): ResultSet
    fun executeUpdate()
    fun set(index: Int, value: UUID) {
        val buf = ByteArray(16)
        set(index, value.toByteArray(buf))
    }
}