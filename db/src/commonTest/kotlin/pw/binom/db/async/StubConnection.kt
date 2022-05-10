package pw.binom.db.async

import pw.binom.db.DatabaseEngine
import pw.binom.db.TransactionMode

class StubConnection : AsyncConnection {
    val db = StubSQL()
    override val type: String
        get() = "PostgreSQL"
    override val isConnected: Boolean
        get() = true
    override val dbInfo: DatabaseInfo = object : DatabaseInfo {
        override val tableNameQuotesStart: String
            get() = "'"
        override val tableNameQuotesEnd: String
            get() = "'"
        override val engine: DatabaseEngine
            get() = DatabaseEngine.POSTGRESQL
    }

    override suspend fun setTransactionMode(mode: TransactionMode) {
        transactionMode = mode
    }

    override var transactionMode: TransactionMode = TransactionMode.READ_COMMITTED
    override suspend fun createStatement(): AsyncStatement = StubAsyncStatement(this)

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement =
        StubAsyncPreparedStatement(this, query)

    override fun isReadyForQuery(): Boolean = true

    override suspend fun beginTransaction() {
    }

    override suspend fun commit() {
    }

    override suspend fun rollback() {
    }

    override suspend fun asyncClose() {
    }
}
