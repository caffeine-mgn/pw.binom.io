package pw.binom.db

import pw.binom.io.Closeable

interface Connection : Closeable {
    fun createStatement(): Statement
    fun prepareStatement(query:String):PreparedStatement
}