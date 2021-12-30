package pw.binom.db.sqlite

import kotlinx.coroutines.withContext
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.*
import pw.binom.db.TransactionMode
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.doFreeze
import pw.binom.io.use
import pw.binom.neverFreeze

class AsyncConnectionAdapter private constructor(val worker: Worker, val connection: SyncConnection) :
    AsyncConnection {
    companion object {
        suspend fun create(creator: () -> SyncConnection): AsyncConnectionAdapter {
            val w = Worker()
            return AsyncConnectionAdapter(
                worker = w,
                connection = creator()
            )
        }
    }

    internal val busy = AtomicBoolean(false)

    override val isConnected: Boolean
        get() {
            val connection = connection
            return worker.execute {
                connection.isConnected
            }.joinAndGetOrThrow()
        }

    override val dbInfo: DatabaseInfo
        get() = SQLiteSQLDatabaseInfo

    override suspend fun setTransactionMode(mode: TransactionMode) {
        val connection = connection
        when (mode) {
            TransactionMode.SERIALIZABLE -> {
                worker.execute {
                    connection.createStatement().use {
                        it.executeQuery("PRAGMA read_uncommitted = false;")
                    }
                }
            }
            TransactionMode.READ_UNCOMMITTED -> {
                worker.execute {
                    connection.createStatement().use {
                        it.executeQuery("PRAGMA read_uncommitted = true;")
                    }
                }
            }
            else -> throw IllegalArgumentException("SQLite not support transaction isolation mode $mode")
        }
        _transactionMode = mode
    }

    private var _transactionMode: TransactionMode = TransactionMode.SERIALIZABLE

    override val transactionMode: TransactionMode
        get() = _transactionMode

    override val type: String
        get() {
            val connection = connection
            return worker.execute {
                connection.type
            }.joinAndGetOrThrow()
        }

    override suspend fun asyncClose() {
        val connection = connection
        worker.execute {
            connection.close()
        }
        connection.close()
    }

    override suspend fun commit() {
        if (!transactionStarted) {
            throw IllegalStateException("Transaction not started")
        }
        val connection = connection
        worker.execute {
            connection.commit()
        }
        transactionStarted = false
    }

    override suspend fun createStatement(): AsyncStatement {
        val connection = connection
        val result = withContext(worker) {
            connection.createStatement()
        }
        return AsyncStatementAdapter(
            ref = result,
            worker = worker,
            connection = this
        )
    }

    override fun isReadyForQuery(): Boolean =
        !busy.value

    private var transactionStarted = false

    override suspend fun beginTransaction() {
        if (transactionStarted) {
            throw IllegalStateException("Transaction already started")
        }
        val connection = connection
        withContext(worker) {
            connection.createStatement().use {
                it.connection.beginTransaction()
//                it.executeUpdate("begin")
            }
        }
        transactionStarted = true
    }

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement {
        val connection = connection
        val ref = withContext(worker) {
            connection.prepareStatement(query)
        }
        return AsyncPreparedStatementAdapter(
            ref = ref,
            worker = worker,
            connection = this
        )
    }

    override suspend fun rollback() {
        if (!transactionStarted) {
            throw IllegalStateException("Transaction not started")
        }
        val connection = connection
        withContext(worker) {
            connection.rollback()
        }
        transactionStarted = false
    }

    init {
        neverFreeze()
    }
}