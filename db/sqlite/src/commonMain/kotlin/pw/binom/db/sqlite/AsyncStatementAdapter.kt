package pw.binom.db.sqlite

import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.AsyncStatement
import pw.binom.db.sync.SyncStatement

class AsyncStatementAdapter(
    val ref: SyncStatement,
    val worker: MyWorker,
    override val connection: AsyncConnection,
) : AsyncStatement {
    override suspend fun executeQuery(query: String): AsyncResultSet {
        val v = withTimeout(ASYNC_TIMEOUT) {
            withContext(worker) {
                val r = ref.executeQuery(query)
                r to r.columns
            }
        }

        return AsyncResultSetAdapter(
            ref = v.first,
            worker = worker,
            columns = v.second
        )
    }

    override suspend fun executeUpdate(query: String): Long = withTimeout(ASYNC_TIMEOUT) {
        withContext(worker) {
            ref.executeUpdate(query)
        }
    }

    override suspend fun asyncClose() {
        withTimeout(ASYNC_TIMEOUT) {
            withContext(worker) {
                ref.close()
            }
        }
    }
}
