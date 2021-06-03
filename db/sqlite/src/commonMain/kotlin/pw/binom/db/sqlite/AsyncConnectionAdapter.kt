package pw.binom.db.sqlite

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.*
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.db.sync.SyncConnection
import pw.binom.doFreeze

class AsyncConnectionAdapter private constructor(val worker: Worker, val connection: Reference<SyncConnection>) :
    AsyncConnection {
    companion object {
        suspend fun create(creator: () -> SyncConnection): AsyncConnectionAdapter {
            val w = Worker()
            creator.doFreeze()
            val ref = execute(w) {
                creator().asReference()
            }
            return AsyncConnectionAdapter(
                worker = w,
                connection = ref
            )
        }
    }

    internal val busy = AtomicBoolean(false)

    override val isConnected: Boolean
        get() =
            worker.execute(this) {
                it.connection.value.isConnected
            }.joinAndGetOrThrow()

    override val type: String
        get() =
            worker.execute(this) {
                it.connection.value.type
            }.joinAndGetOrThrow()

    override suspend fun asyncClose() {
        execute(worker) {
            connection.close()
        }
        connection.close()
    }

    override suspend fun commit() {
        execute(worker) {
            connection.value.commit()
        }
    }

    override suspend fun createStatement(): AsyncStatement {
        val result = execute(worker) {
            connection.value.createStatement().asReference()
        }
        return AsyncStatementAdapter(
            ref = result,
            worker = worker,
            connection = this
        )
    }

    override fun isReadyForQuery(): Boolean =
        !busy.value

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement {
        val ref = execute(worker) {
            connection.value.prepareStatement(query).asReference()
        }
        return AsyncPreparedStatementAdapter(
            ref = ref,
            worker = worker,
            connection = this
        )
    }

    override suspend fun rollback() {
        execute(worker) {
            connection.value.rollback()
        }
    }
}