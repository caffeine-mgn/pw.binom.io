package pw.binom.db

import pw.binom.io.AsyncCloseable

interface AsyncConnection : AsyncCloseable {
    fun createStatement(): Statement
    fun prepareStatement(query: String): AsyncPreparedStatement
    fun commit()
    fun rollback()
    val type: String
}