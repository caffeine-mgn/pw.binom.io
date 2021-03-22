package pw.binom.db.postgresql.async

import pw.binom.db.*

class PostgreAsyncStatement(override val connection: PGConnection) : AsyncStatement {
    internal var lastOpenResultSet: PostgresAsyncResultSet? = null

    private suspend fun checkClosePreviousResultSet() {
        lastOpenResultSet?.let { if (!it.isClosed) it.asyncClose() }
        lastOpenResultSet = null
    }

    override suspend fun executeQuery(query: String): AsyncResultSet {
        checkClosePreviousResultSet()
        val response = connection.query(query)
        if (response is QueryResponse.Data) {
            val q = PostgresAsyncResultSet(true, response)
            lastOpenResultSet = q
            return q
        }
        throw SQLException("Query doesn't return data")
    }

    override suspend fun executeUpdate(query: String): Long {
        checkClosePreviousResultSet()
        val response = connection.query(query)
        if (response is QueryResponse.Status) {
            return response.rowsAffected
        }
        if (response is QueryResponse.Data) {
            response.asyncClose()
        }

        throw SQLException("Query returns data")
    }

    override suspend fun asyncClose() {
        checkClosePreviousResultSet()
    }

}