package pw.binom.db.sqlite

import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import platform.posix.strlen
import pw.binom.atomic.AtomicBoolean
import pw.binom.date.DateTime
import pw.binom.db.SQLException
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.db.sync.SyncResultSet
import pw.binom.io.ClosedException

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
class SQLitePrepareStatement(
  override val connection: SQLiteConnector,
  internal val native: CPointer<CPointerVar<sqlite3_stmt>>,
  query: String,
) : SyncPreparedStatement {

  internal var openedResultSetCount = 0
  private val closed = AtomicBoolean(false)

  private fun checkClosed() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

  private inline val stmt
    get() = native[0]

  private inline val maxParams
    get() = sqlite3_limit(connection.ctx.pointed.value, SQLITE_LIMIT_VARIABLE_NUMBER, -1)

  private val stringBinds = HashMap<Int, String>()

  private inline fun checkRange(index: Int) {
    if (index < 0 || index >= maxParams) {
      throw IndexOutOfBoundsException()
    }
  }

//    override fun set(index: Int, value: BigInteger) {
//        checkClosed()
//        checkRange(index)
//        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value.toString(), -1, null))
//    }
//
//    override fun set(index: Int, value: BigDecimal) {
//        checkClosed()
//        checkRange(index)
//        connection.checkSqlCode(sqlite3_bind_text(stmt, index + 1, value.toString(), -1, null))
//    }

  override fun set(index: Int, value: Double) {
    checkClosed()
    checkRange(index)
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_double(stmt, index + 1, value))
  }

  override fun set(index: Int, value: Float) {
    checkClosed()
    checkRange(index)
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_double(stmt, index + 1, value.toDouble()))
  }

  override fun set(index: Int, value: Int) {
    checkClosed()
    checkRange(index)
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_int(stmt, index + 1, value))
  }

  override fun set(index: Int, value: Long) {
    checkClosed()
    checkRange(index)
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_int64(stmt, index + 1, value))
  }

  override fun set(index: Int, value: String) {
    checkClosed()
    checkRange(index)
    stringBinds[index] = value
  }

  override fun set(index: Int, value: Boolean) {
    checkClosed()
    stringBinds.remove(index)
    set(index, if (value) 1 else 0)
  }

  override fun set(index: Int, value: ByteArray) {
    checkClosed()
    checkRange(index)
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_blob(stmt, index + 1, value.refTo(0), value.size, SQLITE_STATIC))
  }

  override fun set(index: Int, value: DateTime) {
    checkClosed()
    stringBinds.remove(index)
    set(index, value.time)
  }

  override fun executeQuery(): SyncResultSet {
    checkClosed()
    val code = prepareParams {
      sqlite3_step(stmt)
    }
    connection.checkSqlCode(code)
    val result = when (code) {
      SQLITE_DONE -> SQLiteResultSet(this, true)
      SQLITE_ROW -> SQLiteResultSet(this, false)
      else -> throw IllegalStateException()
    }
    openedResultSetCount++
    return result
  }

  private inline fun <T> prepareParams(func: () -> T): T =
    memScoped {
      stringBinds.forEach {
        val len = strlen(it.value)
        connection.checkSqlCode(
          sqlite3_bind_text(
            stmt,
            it.key + 1,
            it.value,
            len.convert(),
            SQLITE_STATIC,
          ),
        )
      }
      func()
    }

  override fun executeUpdate(): Long {
    checkClosed()
    val before = sqlite3_total_changes(connection.ctx.pointed.value)
    prepareParams {
      val code = sqlite3_step(stmt)

      connection.checkSqlCode(code)
      val rownum = sqlite3_column_int64(stmt, 0)
      sqlite3_reset(stmt)
    }
    return (sqlite3_total_changes(connection.ctx.pointed.value) - before).toLong()
  }

  override fun setNull(index: Int) {
    checkClosed()
    stringBinds.remove(index)
    connection.checkSqlCode(sqlite3_bind_null(stmt, index + 1))
  }

  override fun close() {
    if (openedResultSetCount > 0) {
      throw SQLException("Not all ResultSet closed")
    }
    if (!closed.compareAndSet(false, true)) {
      return
    }
    stringBinds.clear()
    connection.delete(this)
    sqlite3_clear_bindings(stmt)
    sqlite3_finalize(stmt)
    nativeHeap.free(native)
  }
}
