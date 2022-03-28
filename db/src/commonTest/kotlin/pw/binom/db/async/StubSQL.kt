package pw.binom.db.async

import kotlinx.coroutines.delay

class StubSQL {
    suspend fun select(sql: String): StubAsyncResultSet {
        if (sql.lowercase().trim() == "select 1") {
            return StubAsyncResultSet.EMPTY
        }
        TODO("Not yet implemented")
    }

    suspend fun update(sql: String): Long {
        val sql = sql.trim()
        if (sql.startsWith("sleep ")) {
            val time = sql.removePrefix("sleep ").trim().toLong()
            println("Try delay $time ms")
            delay(time)
            return 0
        }
        throw IllegalArgumentException("Invalid SQL")
    }
}
