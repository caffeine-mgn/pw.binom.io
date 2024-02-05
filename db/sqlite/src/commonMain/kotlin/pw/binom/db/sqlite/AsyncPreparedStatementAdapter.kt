package pw.binom.db.sqlite

import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import pw.binom.date.DateTime
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.uuid.UUID
import kotlin.coroutines.CoroutineContext

class AsyncPreparedStatementAdapter(
  val ref: SyncPreparedStatement,
  val ctx: CoroutineContext,
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

  override suspend fun set(
    index: Int,
    value: Double,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: Float,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: Int,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: Short,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: Long,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: String,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: Boolean,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: ByteArray,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: DateTime,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun setNull(index: Int) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.setNull(index)
      }
    }
  }

  override suspend fun executeQuery(): AsyncResultSet {
    val ref = ref
    val out =
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          val r = ref.executeQuery()
          r to r.columns
        }
      }
    return AsyncResultSetAdapter(
      ref = out.first,
      context = ctx,
      columns = out.second,
    )
  }

  override suspend fun setValue(
    index: Int,
    value: Any?,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.setValue(index, value)
      }
    }
  }

  override suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
    val ref = ref
    val out =
      withTimeout(ASYNC_TIMEOUT) {
        withContext(ctx) {
          val r = ref.executeQuery(*arguments)
          r to r.columns
        }
      }
    return AsyncResultSetAdapter(
      ref = out.first,
      context = ctx,
      columns = out.second,
    )
  }

  override suspend fun executeUpdate(vararg arguments: Any?): Long {
    val ref = ref
    return withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.executeUpdate(*arguments)
      }
    }
  }

  override suspend fun set(
    index: Int,
    value: UUID,
  ) {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.set(index, value)
      }
    }
  }

  override suspend fun executeUpdate(): Long {
    val ref = ref
    return withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.executeUpdate()
      }
    }
  }

  override suspend fun asyncClose() {
    val ref = ref
    withTimeout(ASYNC_TIMEOUT) {
      withContext(ctx) {
        ref.close()
      }
    }
  }
}
