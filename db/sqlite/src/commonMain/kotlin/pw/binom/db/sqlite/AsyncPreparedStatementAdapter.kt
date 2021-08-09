package pw.binom.db.sqlite

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.UUID
import pw.binom.concurrency.Reference
import pw.binom.concurrency.Worker
import pw.binom.concurrency.asReference
import pw.binom.concurrency.execute
import pw.binom.date.Date
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.doFreeze

class AsyncPreparedStatementAdapter(
    val ref: Reference<SyncPreparedStatement>,
    val worker: Worker,
    override val connection: AsyncConnection,
) : AsyncPreparedStatement {

    init {
        doFreeze()
    }

    override suspend fun set(index: Int, value: BigInteger) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: BigDecimal) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Double) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Float) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Int) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Long) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: String) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Boolean) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: ByteArray) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Date) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun setNull(index: Int) {
        execute(worker) {
            ref.value.setNull(index)
        }
    }

    override suspend fun executeQuery(): AsyncResultSet {
        val out = execute(worker) {
            val r = ref.value.executeQuery()
            r.asReference() to r.columns
        }
        return AsyncResultSetAdapter(
            ref = out.first,
            worker = worker,
            columns = out.second
        )
    }

    override suspend fun setValue(index: Int, value: Any?) {
        execute(worker) {
            ref.value.setValue(index, value)
        }
    }

    override suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.doFreeze()
        val out = execute(worker) {
            val r = ref.value.executeQuery(*arguments)
            r.asReference() to r.columns
        }
        return AsyncResultSetAdapter(
            ref = out.first,
            worker = worker,
            columns = out.second
        )
    }

    override suspend fun executeUpdate(vararg arguments: Any?): Long {
        arguments.doFreeze()
        return execute(worker) {
            ref.value.executeUpdate(*arguments)
        }
    }

    override suspend fun set(index: Int, value: UUID) {
        execute(worker) {
            ref.value.set(index, value)
        }
    }

    override suspend fun executeUpdate(): Long =
        execute(worker) {
            ref.value.executeUpdate()
        }

    override suspend fun asyncClose() {
        execute(worker) {
            ref.value.close()
        }
        ref.close()
    }
}