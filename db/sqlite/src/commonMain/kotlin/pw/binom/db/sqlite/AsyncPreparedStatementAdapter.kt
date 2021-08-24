package pw.binom.db.sqlite

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.UUID
import pw.binom.concurrency.*
import pw.binom.coroutine.start
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
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: BigDecimal) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Double) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Float) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Int) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Long) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: String) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Boolean) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: ByteArray) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Date) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun setNull(index: Int) {
        worker.start {
            ref.value.setNull(index)
        }
    }

    override suspend fun executeQuery(): AsyncResultSet {
        val out = worker.start {
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
        worker.start {
            ref.value.setValue(index, value)
        }
    }

    override suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.doFreeze()
        val out = worker.start {
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
        return worker.start {
            ref.value.executeUpdate(*arguments)
        }
    }

    override suspend fun set(index: Int, value: UUID) {
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun executeUpdate(): Long =
        worker.start {
            ref.value.executeUpdate()
        }

    override suspend fun asyncClose() {
        worker.start {
            ref.value.close()
        }
        ref.close()
    }
}