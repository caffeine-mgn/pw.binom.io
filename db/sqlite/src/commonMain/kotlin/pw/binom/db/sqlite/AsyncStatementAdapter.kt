package pw.binom.db.sqlite

import pw.binom.concurrency.*
import pw.binom.coroutine.start
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
        val v = worker.start {
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
        worker.start {
            ref.value.executeUpdate(query)
        }

    override suspend fun asyncClose() {
        worker.start {
            ref.value.close()
        }
        ref.close()
    }


}