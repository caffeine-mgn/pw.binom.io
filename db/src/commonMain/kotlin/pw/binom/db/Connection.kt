package pw.binom.db

import pw.binom.io.Closeable

interface Connection : Closeable {
    fun createStatement(): Statement
    fun prepareStatement(query: String): PreparedStatement
    fun commit()
    fun rollback()
    val type: String
}