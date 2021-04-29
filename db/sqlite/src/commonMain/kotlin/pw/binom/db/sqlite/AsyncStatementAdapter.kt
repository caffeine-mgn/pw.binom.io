package pw.binom.db.sqlite

import pw.binom.concurrency.Reference
import pw.binom.concurrency.Worker
import pw.binom.concurrency.asReference
import pw.binom.concurrency.execute
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.AsyncStatement
import pw.binom.db.sync.SyncStatement

class AsyncStatementAdapter(
    val ref: Reference<SyncStatement>,
    val worker: Worker,
    override val connection: AsyncConnection,
) : AsyncStatement {
    override suspend fun executeQuery(query: String): AsyncResultSet {
        val v = execute(worker) {
            val r = ref.value.executeQuery(query)
            r.asReference() to r.columns
        }

        return AsyncResultSetAdapter(
            ref = v.first,
            worker = worker,
            columns = v.second
        )
    }

    override suspend fun executeUpdate(query: String): Long =
        execute(worker) {
            ref.value.executeUpdate(query)
        }

    override suspend fun asyncClose() {
        execute(worker) {
            ref.value.close()
        }
        ref.close()
    }


}