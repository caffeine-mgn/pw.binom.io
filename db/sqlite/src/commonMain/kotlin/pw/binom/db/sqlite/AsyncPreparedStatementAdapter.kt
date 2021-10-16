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
import pw.binom.neverFreeze

class AsyncPreparedStatementAdapter(
    val ref: Reference<SyncPreparedStatement>,
    val worker: Worker,
    override val connection: AsyncConnection,
) : AsyncPreparedStatement {

    init {
        neverFreeze()
    }

    override suspend fun set(index: Int, value: BigInteger) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: BigDecimal) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Double) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Float) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Int) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Long) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: String) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Boolean) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: ByteArray) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun set(index: Int, value: Date) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun setNull(index: Int) {
        val ref = ref
        worker.start {
            ref.value.setNull(index)
        }
    }

    override suspend fun executeQuery(): AsyncResultSet {
        val ref = ref
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
        val ref = ref
        worker.start {
            ref.value.setValue(index, value)
        }
    }

    override suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.doFreeze()
        val ref = ref
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
        val ref = ref
        return worker.start {
            ref.value.executeUpdate(*arguments)
        }
    }

    override suspend fun set(index: Int, value: UUID) {
        val ref = ref
        worker.start {
            ref.value.set(index, value)
        }
    }

    override suspend fun executeUpdate(): Long {
        val ref = ref
        return worker.start {
            ref.value.executeUpdate()
        }
    }

    override suspend fun asyncClose() {
        val ref = ref
        worker.start {
            ref.value.close()
        }
        ref.close()
    }
}