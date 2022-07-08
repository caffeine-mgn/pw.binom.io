package pw.binom.db.sqlite

import kotlinx.coroutines.withContext
import pw.binom.UUID
import pw.binom.concurrency.Worker
import pw.binom.date.DateTime
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.doFreeze

class AsyncPreparedStatementAdapter(
    val ref: SyncPreparedStatement,
    val worker: Worker,
    override val connection: AsyncConnection,
) : AsyncPreparedStatement {

//    override suspend fun set(index: Int, value: BigInteger) {
//        val ref = ref
//        withContext(worker) {
//            ref.set(index, value)
//        }
//    }
//
//    override suspend fun set(index: Int, value: BigDecimal) {
//        val ref = ref
//        withContext(worker) {
//            ref.set(index, value)
//        }
//    }

    override suspend fun set(index: Int, value: Double) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Float) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Int) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Long) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: String) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Boolean) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: ByteArray) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: DateTime) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun setNull(index: Int) {
        val ref = ref
        withContext(worker) {
            ref.setNull(index)
        }
    }

    override suspend fun executeQuery(): AsyncResultSet {
        val ref = ref
        val out = withContext(worker) {
            val r = ref.executeQuery()
            r to r.columns
        }
        return AsyncResultSetAdapter(
            ref = out.first,
            worker = worker,
            columns = out.second
        )
    }

    override suspend fun setValue(index: Int, value: Any?) {
        val ref = ref
        withContext(worker) {
            ref.setValue(index, value)
        }
    }

    override suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.doFreeze()
        val ref = ref
        val out = withContext(worker) {
            val r = ref.executeQuery(*arguments)
            r to r.columns
        }
        return AsyncResultSetAdapter(
            ref = out.first,
            worker = worker,
            columns = out.second
        )
    }

    override suspend fun executeUpdate(vararg arguments: Any?): Long {
        arguments.doFreeze()
        val ref = ref
        return withContext(worker) {
            ref.executeUpdate(*arguments)
        }
    }

    override suspend fun set(index: Int, value: UUID) {
        val ref = ref
        withContext(worker) {
            ref.set(index, value)
        }
    }

    override suspend fun executeUpdate(): Long {
        val ref = ref
        return withContext(worker) {
            ref.executeUpdate()
        }
    }

    override suspend fun asyncClose() {
        val ref = ref
        withContext(worker) {
            ref.close()
        }
    }
}
