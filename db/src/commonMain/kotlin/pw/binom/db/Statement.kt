package pw.binom.db

import pw.binom.io.Closeable

interface Statement : Closeable {
    val connection: Connection
    fun executeQuery(query: String): ResultSet
    fun executeUpdate(query: String)
}