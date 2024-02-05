@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.db.sqlite

import kotlinx.coroutines.*
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.db.TransactionMode
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.io.Closeable
import pw.binom.io.use
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

internal val ASYNC_TIMEOUT = 1.minutes

class SqlLiteAsyncWorker : CoroutineDispatcher(), Closeable {
  private val readyForWriteListener = BatchExchange<Runnable>()

  override fun isDispatchNeeded(context: CoroutineContext): Boolean = Thread.currentThread !== thread

  private val lock = ReentrantLock()
  private val condition = lock.newCondition()
  private val closed = AtomicBoolean(false)

  private val thread =
    Thread { thread: Thread ->
      while (!closed.getValue()) {
        lock.synchronize {
          if (readyForWriteListener.isEmpty()) {
            condition.await()
          }
          if (closed.getValue()) {
            return@synchronize
          }
          readyForWriteListener.popAll {
            it.forEach {
              try {
                it.run()
              } catch (e: Throwable) {
                thread.uncaughtExceptionHandler.uncaughtException(
                  thread = thread,
                  throwable = e,
                )
              }
            }
          }
        }
      }
    }

  init {
    thread.start()
  }

  override fun dispatch(
    context: CoroutineContext,
    block: Runnable,
  ) {
    readyForWriteListener.push(block)
    lock.synchronize {
      condition.signalAll()
    }
  }

//    fun <T> execute(func: () -> T): T {
//        var result: T? = null
//        val lock = ReentrantLock()
//        val con = lock.newCondition()
//        r.synchronize {
//            readyForWriteListener.push(
//                Runnable {
//                    lock.synchronize {
//                        result = func()
//                        con.signalAll()
//                    }
//                }
//            )
//            c.await()
//        }
//        return result as T
//    }

  override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    lock.synchronize {
      condition.signalAll()
    }
    if (thread.id != Thread.currentThread.id) {
      thread.join()
    }
  }
}

class AsyncConnectionAdapter private constructor(/*val worker: Worker, */val connection: SyncConnection) :
  AsyncConnection {
    companion object {
      fun create(creator: () -> SyncConnection): AsyncConnectionAdapter =
        AsyncConnectionAdapter(
          connection = creator(),
        )
    }

    private val worker = SqlLiteAsyncWorker()
    private val ctx = worker + CoroutineName("SQLite")

    internal val busy = AtomicBoolean(false)

    override val isConnected: Boolean
      get() = connection.isConnected

    override val dbInfo: DatabaseInfo
      get() = SQLiteSQLDatabaseInfo

    override suspend fun setTransactionMode(mode: TransactionMode) {
      val connection = connection
      when (mode) {
        TransactionMode.SERIALIZABLE -> {
          withTimeout(ASYNC_TIMEOUT) {
            withContext(ctx) {
              connection.createStatement().use {
                it.executeQuery("PRAGMA read_uncommitted = false;")
              }
            }
          }
        }

        TransactionMode.READ_UNCOMMITTED -> {
          withTimeout(ASYNC_TIMEOUT) {
            withContext(ctx) {
              connection.createStatement().use {
                it.executeQuery("PRAGMA read_uncommitted = true;")
              }
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
      get() = connection.type

    override suspend fun asyncClose() {
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          connection.close()
        }
      }
//        val connection = connection
//        worker.execute {
//            connection.close()
//        }
//        connection.close()
    }

    override suspend fun commit() {
      if (!transactionStarted) {
        throw IllegalStateException("Transaction not started")
      }
      val connection = connection
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          connection.commit()
        }
      }
      transactionStarted = false
    }

    override suspend fun createStatement(): AsyncStatement {
      val connection = connection
      val result =
        withTimeout(ASYNC_TIMEOUT) {
          withContext(ctx) {
            connection.createStatement()
          }
        }
      return AsyncStatementAdapter(
        ref = result,
        context = ctx,
        connection = this,
      )
    }

    override fun isReadyForQuery(): Boolean = !busy.getValue()

    private var transactionStarted = false

    override suspend fun beginTransaction() {
      if (transactionStarted) {
        throw IllegalStateException("Transaction already started")
      }
      val connection = connection
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          connection.createStatement().use {
            it.connection.beginTransaction()
//                it.executeUpdate("begin")
          }
        }
      }
      transactionStarted = true
    }

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement {
      val connection = connection
      val ref =
        withTimeout(ASYNC_TIMEOUT) {
          withContext(ctx) {
            connection.prepareStatement(query)
          }
        }
      return AsyncPreparedStatementAdapter(
        ref = ref,
        ctx = ctx,
        connection = this,
      )
    }

    override suspend fun rollback() {
      if (!transactionStarted) {
        throw IllegalStateException("Transaction not started")
      }
      val connection = connection
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          connection.rollback()
        }
      }
      transactionStarted = false
    }
  }
