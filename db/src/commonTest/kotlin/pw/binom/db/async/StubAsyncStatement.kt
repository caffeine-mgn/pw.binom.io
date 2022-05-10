package pw.binom.db.async

class StubAsyncStatement(override val connection: StubConnection) : AsyncStatement {
    override suspend fun executeQuery(query: String): AsyncResultSet = connection.db.select(query)

    override suspend fun executeUpdate(query: String): Long = connection.db.update(query)

    override suspend fun asyncClose() {
        TODO("Not yet implemented")
    }
}
