package pw.binom.db.postgresql.async

import pw.binom.db.*

class PostgreAsyncStatement(override val connection: PGConnection) : AsyncStatement {
    override suspend fun executeQuery(query: String): AsyncResultSet {
        val response = connection.query(query)
        if (response is QueryResponse.Data) {
            return PostgresAsyncResultSet(true, response)
        }
        throw SQLException("Query doesn't return data")
    }

    override suspend fun executeUpdate(query: String): Long {
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
    }

}